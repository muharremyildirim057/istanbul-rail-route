package project.istanbulrailroute.application.routing.impl;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.fare.LineFareFactory;
import project.istanbulrailroute.domain.models.StationConnection;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.infrastructure.StationRepository;
import project.istanbulrailroute.domain.models.enums.StationStatus;
import project.istanbulrailroute.presentation.dto.routeDto.RouteResponseDto;

import java.util.*;

@Service("shortestTimeStrategy")
public class ShortestTimeRouteStrategy extends project.istanbulrailroute.application.routing.impl.AbstractRouteStrategy {

    public ShortestTimeRouteStrategy(StationConnectionRepository connectionRepository,
                                     StationRepository stationRepository,
                                     LineFareFactory lineFareFactory) {
        super(connectionRepository, stationRepository, lineFareFactory);
    }

    @Override
    public RouteResponseDto calculateRoute(Long startStationId, Long endStationId) {
        List<StationConnection> allConnections = connectionRepository.findAll();
        Map<Long, List<StationConnection>> graph = new HashMap<>();
        for (StationConnection conn : allConnections) {
            graph.computeIfAbsent(conn.getStartStation().getId(), k -> new ArrayList<>()).add(conn);
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousStates = new HashMap<>();
        Set<String> visited = new HashSet<>();

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        String startState = startStationId + "_null";
        distances.put(startState, 0.0);
        pq.add(new NodeDistance(startStationId, 0.0, null));

        String bestEndState = null;
        double exactDijkstraTime = 0.0; // DIJKSTRA'NIN BULDUĞU KESİN SÜRE

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Long currentNode = current.nodeId;
            String currentState = currentNode + "_" + current.lineId;

            if (currentNode.equals(endStationId)) {
                bestEndState = currentState;
                exactDijkstraTime = current.distance;
                break;
            }

            if (visited.contains(currentState)) continue;
            visited.add(currentState);

            List<StationConnection> neighbors = graph.getOrDefault(currentNode, new ArrayList<>());

            boolean isCurrentStationTransitOnly = false;
            if (!neighbors.isEmpty()) {
                StationStatus status = neighbors.get(0).getStartStation().getStatus();
                if (status == StationStatus.MAINTENANCE || status == StationStatus.DISRUPTED) {
                    isCurrentStationTransitOnly = true;
                }
            }

            for (StationConnection edge : neighbors) {
                StationStatus targetStatus = edge.getTargetStation().getStatus();

                if (targetStatus == StationStatus.UNDER_CONSTRUCTION || targetStatus == StationStatus.DECOMMISSIONED) continue;

                Long neighborNode = edge.getTargetStation().getId();
                Long edgeLineId = edge.getLine().getId();

                if (isCurrentStationTransitOnly && current.lineId != null && !current.lineId.equals(edgeLineId)) continue;

                String neighborState = neighborNode + "_" + edgeLineId;
                if (visited.contains(neighborState)) continue;

                double weight = edge.getDuration();
                if (current.lineId != null && !current.lineId.equals(edgeLineId)) {
                    weight += 5.0; // Aktarma bekleme süresi
                }

                double newDist = current.distance + weight;
                if (newDist < distances.getOrDefault(neighborState, Double.MAX_VALUE)) {
                    distances.put(neighborState, newDist);
                    previousStates.put(neighborState, currentState);
                    pq.add(new NodeDistance(neighborNode, newDist, edgeLineId));
                }
            }
        }

        if (bestEndState == null) throw new RuntimeException("Destination station is unreachable or route not found!");

        List<Long> path = new ArrayList<>();
        String currState = bestEndState;
        while (currState != null) {
            String[] parts = currState.split("_");
            Long sId = Long.parseLong(parts[0]);
            if (path.isEmpty() || !path.get(path.size() - 1).equals(sId)) { path.add(sId); }
            currState = previousStates.get(currState);
        }
        Collections.reverse(path);

        // KUSURSUZ ZAMAN ENJEKSİYONU
        RouteResponseDto finalResponse = super.buildResponseDto(path);
        finalResponse.setTotalDuration(exactDijkstraTime);
        return finalResponse;
    }

    @Override
    public String getStrategyName() { return "SHORTEST_TIME"; }

    private static class NodeDistance {
        Long nodeId; double distance; Long lineId;
        NodeDistance(Long nodeId, double distance, Long lineId) {
            this.nodeId = nodeId; this.distance = distance; this.lineId = lineId;
        }
    }
}