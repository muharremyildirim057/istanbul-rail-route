package project.istanbulrailroute.application.routing.impl;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.routing.RouteCalculationStrategy;
import project.istanbulrailroute.domain.models.StationConnection;
import project.istanbulrailroute.domain.models.enums.LineType;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.domain.models.enums.StationStatus;

import java.util.*;

@Service("cheapestFareStrategy")
public class CheapestFareRouteStrategy implements RouteCalculationStrategy {

    private final StationConnectionRepository connectionRepository;

    public CheapestFareRouteStrategy(StationConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public List<Long> calculateRoute(Long startStationId, Long endStationId) {
        List<StationConnection> allConnections = connectionRepository.findAll();

        Map<Long, List<StationConnection>> graph = new HashMap<>();
        for (StationConnection conn : allConnections) {
            graph.computeIfAbsent(conn.getStartStation().getId(), k -> new ArrayList<>()).add(conn);
        }

        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> previousNodes = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        distances.put(startStationId, 0.0);
        pq.add(new NodeDistance(startStationId, 0.0, null));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Long currentNode = current.nodeId;

            if (currentNode.equals(endStationId)) {
                break;
            }

            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            List<StationConnection> neighbors = graph.getOrDefault(currentNode, new ArrayList<>());

            // AKTARMA KONTROLÜ İÇİN İSTASYONUN DURUMUNU ÖĞRENME
            boolean isCurrentStationClosed = false;
            if (!neighbors.isEmpty()) {
                StationStatus status = neighbors.get(0).getStartStation().getStatus();
                if (status != StationStatus.ACTIVE) {
                    isCurrentStationClosed = true;
                }
            }

            for (StationConnection edge : neighbors) {
                Long neighborNode = edge.getTargetStation().getId();
                if (visited.contains(neighborNode)) continue;

                // KURAL: Eğer durak kapalıysa ve hat değişiyorsa (aktarma yapılıyorsa) bu yolu es geç!
                if (isCurrentStationClosed && current.lineId != null && !current.lineId.equals(edge.getLine().getId())) {
                    continue;
                }

                // ÜCRET (MALİYET) HESAPLAMA MANTIĞI
                double weight = 1.0; // Temel durak geçiş maliyeti

                // Eğer hat Banliyö (Marmaray) ise durak başı ücret artar (Mesafe bazlı ücretlendirme)
                if (edge.getLine().getType() == LineType.BANLIYO) {
                    weight += 2.0;
                }

                // Eğer aktarma yapılıyorsa ek ücret maliyeti (Ağırlığı) biner
                if (current.lineId != null && !current.lineId.equals(edge.getLine().getId())) {
                    weight += 15.0; // Aktarma yapmaktan kaçınması için yüksek maliyet
                }

                double newDist = distances.getOrDefault(currentNode, 0.0) + weight;

                if (newDist < distances.getOrDefault(neighborNode, Double.MAX_VALUE)) {
                    distances.put(neighborNode, newDist);
                    previousNodes.put(neighborNode, currentNode);
                    pq.add(new NodeDistance(neighborNode, newDist, edge.getLine().getId()));
                }
            }
        }

        if (!previousNodes.containsKey(endStationId)) {
            throw new RuntimeException("Destination station is unreachable or route not found!");
        }

        List<Long> path = new ArrayList<>();
        Long curr = endStationId;
        while (curr != null) {
            path.add(curr);
            curr = previousNodes.get(curr);
        }
        Collections.reverse(path);

        return path;
    }

    @Override
    public String getStrategyName() {
        return "CHEAPEST_FARE";
    }

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