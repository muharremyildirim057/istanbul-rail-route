package project.istanbulrailroute.application.routing.impl;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.routing.RouteCalculationStrategy;
import project.istanbulrailroute.domain.models.StationConnection;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;

import java.util.*;

@Service("shortestTimeStrategy")
public class ShortestTimeRouteStrategy implements RouteCalculationStrategy {

    private final StationConnectionRepository connectionRepository;

    public ShortestTimeRouteStrategy(StationConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public List<Long> calculateRoute(Long startStationId, Long endStationId) {
        // 1. Tüm ağı veritabanından RAM'e çekiyoruz (Bu işlem inanılmaz hızlıdır)
        List<StationConnection> allConnections = connectionRepository.findAll();

        // 2. Graf (Ağ) Haritasını Oluşturma (Hangi duraktan nerelere gidilebilir?)
        Map<Long, List<StationConnection>> graph = new HashMap<>();
        for (StationConnection conn : allConnections) {
            graph.computeIfAbsent(conn.getStartStation().getId(), k -> new ArrayList<>()).add(conn);
        }

        // Dijkstra Değişkenleri
        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> previousNodes = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        // Priority Queue: Her zaman en kısa süreli (en avantajlı) yolu önce seçer
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        // Başlangıç durağını ayarla
        distances.put(startStationId, 0.0);
        pq.add(new NodeDistance(startStationId, 0.0, null));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Long currentNode = current.nodeId;

            // Hedefe en kısa yoldan ulaştıysak aramayı bitir!
            if (currentNode.equals(endStationId)) {
                break;
            }

            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            // Komşu durakları gez
            List<StationConnection> neighbors = graph.getOrDefault(currentNode, new ArrayList<>());
            for (StationConnection edge : neighbors) {
                Long neighborNode = edge.getTargetStation().getId();
                if (visited.contains(neighborNode)) continue;

                // Ağırlık = Normal Seyahat Süresi
                double weight = edge.getDuration();

                // AKTARMA CEZASI: Eğer geldiğimiz hat ile gideceğimiz hat farklıysa 5 dk ekle
                if (current.lineId != null && !current.lineId.equals(edge.getLine().getId())) {
                    weight += 5.0;
                }

                double newDist = distances.getOrDefault(currentNode, 0.0) + weight;

                // Eğer bulduğumuz bu yeni yol, öncekilerden daha kısaysa rotayı güncelle
                if (newDist < distances.getOrDefault(neighborNode, Double.MAX_VALUE)) {
                    distances.put(neighborNode, newDist);
                    previousNodes.put(neighborNode, currentNode);
                    pq.add(new NodeDistance(neighborNode, newDist, edge.getLine().getId()));
                }
            }
        }

        if (!previousNodes.containsKey(endStationId)) {
            throw new RuntimeException("Hedef istasyona ulaşılamıyor veya rota bulunamadı!");
        }

        // 3. Bulunan en kısa rotayı sondan başa doğru listeye çevir
        List<Long> path = new ArrayList<>();
        Long curr = endStationId;
        while (curr != null) {
            path.add(curr);
            curr = previousNodes.get(curr);
        }
        Collections.reverse(path); // Rotayı baştan sona doğru düzelt

        return path;
    }

    @Override
    public String getStrategyName() {
        return "SHORTEST_TIME";
    }

    // Algoritmanın kuyrukta tutacağı yardımcı iç sınıf
    private static class NodeDistance {
        Long nodeId;
        double distance;
        Long lineId;

        NodeDistance(Long nodeId, double distance, Long lineId) {
            this.nodeId = nodeId;
            this.distance = distance;
            this.lineId = lineId;
        }
    }
}