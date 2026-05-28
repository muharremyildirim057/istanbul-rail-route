package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.istanbulrailroute.application.routing.RouteCalculationStrategy;
import project.istanbulrailroute.application.fare.LineFareFactory;
import project.istanbulrailroute.application.fare.FareStrategy;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.domain.models.StationConnection;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.infrastructure.StationRepository;
import project.istanbulrailroute.presentation.dto.journeyDto.JourneySegmentDto;
import project.istanbulrailroute.presentation.dto.routeDto.RouteResponseDto;
import project.istanbulrailroute.presentation.dto.routeDto.StationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final List<RouteCalculationStrategy> routingStrategies;
    private final StationRepository stationRepository;
    private final StationConnectionRepository connectionRepository;
    private final LineFareFactory lineFareFactory;
    private final NotificationService notificationService;

    public RouteService(List<RouteCalculationStrategy> routingStrategies,
                        StationRepository stationRepository,
                        StationConnectionRepository connectionRepository,
                        LineFareFactory lineFareFactory,
                        NotificationService notificationService) {
        this.routingStrategies = routingStrategies;
        this.stationRepository = stationRepository;
        this.connectionRepository = connectionRepository;
        this.lineFareFactory = lineFareFactory;
        this.notificationService = notificationService;
    }

    @Transactional
    public RouteResponseDto getOptimalRoute(Long startId, Long endId, String preference) {

        notificationService.checkStationAvailability(startId);
        notificationService.checkStationAvailability(endId);

        RouteCalculationStrategy selectedStrategy = routingStrategies.stream()
                .filter(strategy -> strategy.getStrategyName().equalsIgnoreCase(preference))
                .findFirst()
                .orElseGet(() -> routingStrategies.stream()
                        .filter(s -> s.getStrategyName().equals("SHORTEST_TIME"))
                        .findFirst()
                        .orElse(routingStrategies.get(0)));

        List<Long> routeIds = selectedStrategy.calculateRoute(startId, endId);

        List<Station> unorderedStations = stationRepository.findAllById(routeIds);
        Map<Long, Station> stationMap = unorderedStations.stream()
                .collect(Collectors.toMap(Station::getId, Function.identity()));

        List<StationResponse> orderedRoute = new ArrayList<>();
        for (Long id : routeIds) {
            Station s = stationMap.get(id);
            orderedRoute.add(new StationResponse(s.getId(), s.getName(), s.getStatus()));
        }

        List<JourneySegmentDto> segments = new ArrayList<>();
        Long currentLineId = null;
        String currentLineName = null;
        Line currentLineObj = null;
        int currentStationCount = 0;
        double totalFare = 0.0;
        double totalDuration = 0.0;
        double currentSegmentDuration = 0.0;

        for (int i = 0; i < routeIds.size() - 1; i++) {
            Long fromId = routeIds.get(i);
            Long toId = routeIds.get(i + 1);

            StationConnection conn = connectionRepository.findFirstByStartStationIdAndTargetStationId(fromId, toId)
                    .orElseThrow(() -> new RuntimeException("Error"));

            Long edgeLineId = conn.getLine().getId();
            String edgeLineName = conn.getLine().getName();
            double travelTime = conn.getDuration();

            if (currentLineId == null) {
                currentLineId = edgeLineId;
                currentLineName = edgeLineName;
                currentLineObj = conn.getLine();
                currentStationCount = 1;
                currentSegmentDuration = travelTime;
            } else if (currentLineId.equals(edgeLineId)) {
                currentStationCount++;
                currentSegmentDuration += travelTime;
            } else {
                JourneySegmentDto segment = new JourneySegmentDto();
                segment.setLineId(currentLineId);
                segment.setLineName(currentLineName);
                segment.setStationCount(currentStationCount);
                segment.setSegmentDuration(currentSegmentDuration);

                FareStrategy fareStrategy = lineFareFactory.getFareStrategy(currentLineObj.getType());
                double segmentFare = fareStrategy.calculateFare(currentStationCount);

                if (!segments.isEmpty()) {
                    segmentFare = segmentFare * 0.6;
                    totalDuration += 5.0;
                }

                segment.setSegmentFare(segmentFare);
                totalFare += segmentFare;
                totalDuration += currentSegmentDuration;

                segments.add(segment);

                currentLineId = edgeLineId;
                currentLineName = edgeLineName;
                currentLineObj = conn.getLine();
                currentStationCount = 1;
                currentSegmentDuration = travelTime;
            }
        }

        if (currentLineId != null) {
            JourneySegmentDto segment = new JourneySegmentDto();
            segment.setLineId(currentLineId);
            segment.setLineName(currentLineName);
            segment.setStationCount(currentStationCount);
            segment.setSegmentDuration(currentSegmentDuration);

            FareStrategy fareStrategy = lineFareFactory.getFareStrategy(currentLineObj.getType());
            double segmentFare = fareStrategy.calculateFare(currentStationCount);

            if (!segments.isEmpty()) {
                segmentFare = segmentFare * 0.6;
                totalDuration += 5.0;
            }

            segment.setSegmentFare(segmentFare);
            totalFare += segmentFare;
            totalDuration += currentSegmentDuration;

            segments.add(segment);
        }

        RouteResponseDto response = new RouteResponseDto();
        response.setStations(orderedRoute);
        response.setSegments(segments);
        response.setTotalFare(totalFare);
        response.setTotalDuration(totalDuration);

        return response;
    }
}