package project.istanbulrailroute.application.routing.impl;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.fare.LineFareFactory;
import project.istanbulrailroute.application.fare.FareStrategy;
import project.istanbulrailroute.domain.models.StationConnection;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.infrastructure.StationRepository;
import project.istanbulrailroute.domain.models.enums.StationStatus;
import project.istanbulrailroute.presentation.dto.routeDto.RouteResponseDto;
import project.istanbulrailroute.presentation.dto.routeDto.StationResponse;
import project.istanbulrailroute.presentation.dto.journeyDto.JourneySegmentDto;

import java.util.*;

@Service("cheapestFareStrategy")
public class CheapestFareRouteStrategy extends project.istanbulrailroute.application.routing.impl.AbstractRouteStrategy {

    public CheapestFareRouteStrategy(StationConnectionRepository connectionRepository,
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
        Map<String, StationConnection> incomingEdges = new HashMap<>();
        Set<String> visited = new HashSet<>();

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        // State: NodeId _ LineId _ TransferCount (stationCount ÇIKARILDI!)
        String startState = startStationId + "_null_0";
        distances.put(startState, 0.0);
        pq.add(new NodeDistance(startStationId, 0.0, null, 0, 0));

        String bestEndState = null;

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Long currentNode = current.nodeId;

            // State tanımından stationCount çıkarıldı
            String currentState = currentNode + "_" + current.lineId + "_" + current.transferCount;

            if (currentNode.equals(endStationId)) {
                bestEndState = currentState;
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

                boolean isFirstBoarding = current.lineId == null;
                boolean isTransferring = current.lineId != null && !current.lineId.equals(edgeLineId);

                int newTransferCount = current.transferCount;
                int newStationCount;

                if (isFirstBoarding) {
                    newTransferCount = 0;
                    newStationCount = 1;
                } else if (isTransferring) {
                    if (current.transferCount < 2) {
                        newTransferCount = current.transferCount + 1; // İndirimli aktarma
                    } else {
                        newTransferCount = 0; // Limit doldu, sayaç sıfırlanır
                    }
                    newStationCount = 1;
                } else {
                    newStationCount = current.stationCount + 1;
                }

                FareStrategy fareStrategy = lineFareFactory.getFareStrategy(edge.getLine().getType());

                double newTotalSegmentFare = fareStrategy.calculateFare(newStationCount);
                if (newTransferCount > 0 && newTransferCount <= 2) {
                    newTotalSegmentFare *= 0.6;
                }

                double oldTotalSegmentFare = 0.0;
                if (newStationCount > 1) {
                    oldTotalSegmentFare = fareStrategy.calculateFare(current.stationCount);
                    if (newTransferCount > 0 && newTransferCount <= 2) {
                        oldTotalSegmentFare *= 0.6;
                    }
                }

                // Negatif maliyetleri önlemek için Math.max eklendi
                double edgeCost = Math.max(0.0, newTotalSegmentFare - oldTotalSegmentFare);
                double weight = 0.0001 + edgeCost;

                String neighborState = neighborNode + "_" + edgeLineId + "_" + newTransferCount;

                // Zaten ziyaret edildiyse atla
                if (visited.contains(neighborState)) continue;

                double newDist = current.distance + weight;

                if (newDist < distances.getOrDefault(neighborState, Double.MAX_VALUE)) {
                    distances.put(neighborState, newDist);
                    previousStates.put(neighborState, currentState);
                    incomingEdges.put(neighborState, edge);

                    // Priority Queue içine stationCount'u hesaplamalar için hala gönderiyoruz
                    pq.add(new NodeDistance(neighborNode, newDist, edgeLineId, newStationCount, newTransferCount));
                }
            }
        }

        if (bestEndState == null) {
            throw new RuntimeException("Destination station is unreachable or route not found!");
        }

        List<StationConnection> exactPath = new ArrayList<>();
        String currState = bestEndState;
        while (currState != null && incomingEdges.containsKey(currState)) {
            StationConnection edge = incomingEdges.get(currState);
            exactPath.add(edge);
            currState = previousStates.get(currState);
        }
        Collections.reverse(exactPath);

        return buildExactResponseDto(exactPath);
    }

    private RouteResponseDto buildExactResponseDto(List<StationConnection> exactPath) {
        RouteResponseDto response = new RouteResponseDto();
        if (exactPath.isEmpty()) return response;

        List<StationResponse> stations = new ArrayList<>();
        List<JourneySegmentDto> segments = new ArrayList<>();

        Station startNode = exactPath.get(0).getStartStation();
        stations.add(new StationResponse(startNode.getId(), startNode.getName(), startNode.getStatus()));

        double totalFare = 0.0;
        double totalDuration = 0.0;
        int transferCount = 0;

        Long currentLineId = null;
        int currentStationCount = 0;
        double currentSegmentDuration = 0.0;
        Line currentLineObj = null;

        for (StationConnection conn : exactPath) {
            Station target = conn.getTargetStation();
            stations.add(new StationResponse(target.getId(), target.getName(), target.getStatus()));

            if (currentLineId == null) {
                currentLineId = conn.getLine().getId();
                currentLineObj = conn.getLine();
                currentStationCount = 1;
                currentSegmentDuration = conn.getDuration();
            } else if (currentLineId.equals(conn.getLine().getId())) {
                currentStationCount++;
                currentSegmentDuration += conn.getDuration();
            } else {
                JourneySegmentDto segment = new JourneySegmentDto();
                segment.setLineId(currentLineId);
                segment.setLineName(currentLineObj.getName());
                segment.setStationCount(currentStationCount);
                segment.setSegmentDuration(currentSegmentDuration);

                double baseSegmentFare = lineFareFactory.getFareStrategy(currentLineObj.getType()).calculateFare(currentStationCount);
                if (!segments.isEmpty()) {
                    if (transferCount < 2) {
                        baseSegmentFare *= 0.6;
                        transferCount++;
                    } else {
                        transferCount = 0;
                    }
                    totalDuration += 5.0;
                }

                segment.setSegmentFare(baseSegmentFare);
                totalFare += baseSegmentFare;
                totalDuration += currentSegmentDuration;
                segments.add(segment);

                currentLineId = conn.getLine().getId();
                currentLineObj = conn.getLine();
                currentStationCount = 1;
                currentSegmentDuration = conn.getDuration();
            }
        }

        if (currentLineId != null) {
            JourneySegmentDto segment = new JourneySegmentDto();
            segment.setLineId(currentLineId);
            segment.setLineName(currentLineObj.getName());
            segment.setStationCount(currentStationCount);
            segment.setSegmentDuration(currentSegmentDuration);

            double baseSegmentFare = lineFareFactory.getFareStrategy(currentLineObj.getType()).calculateFare(currentStationCount);
            if (!segments.isEmpty()) {
                if (transferCount < 2) {
                    baseSegmentFare *= 0.6;
                    transferCount++;
                } else {
                    transferCount = 0;
                }
                totalDuration += 5.0;
            }
            segment.setSegmentFare(baseSegmentFare);
            totalFare += baseSegmentFare;
            totalDuration += currentSegmentDuration;
            segments.add(segment);
        }

        response.setStations(stations);
        response.setSegments(segments);
        response.setTotalFare(totalFare);
        response.setTotalDuration(totalDuration);

        return response;
    }

    @Override
    public String getStrategyName() {
        return "CHEAPEST_FARE";
    }

    private static class NodeDistance {
        Long nodeId; double distance; Long lineId; int stationCount; int transferCount;
        NodeDistance(Long nodeId, double distance, Long lineId, int stationCount, int transferCount) {
            this.nodeId = nodeId; this.distance = distance; this.lineId = lineId; this.stationCount = stationCount; this.transferCount = transferCount;
        }
    }
}