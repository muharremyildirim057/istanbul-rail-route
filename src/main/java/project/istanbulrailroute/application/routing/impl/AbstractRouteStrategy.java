package project.istanbulrailroute.application.routing.impl;

import project.istanbulrailroute.application.routing.RouteCalculationStrategy;
import project.istanbulrailroute.application.fare.LineFareFactory;
import project.istanbulrailroute.application.fare.FareStrategy;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.domain.models.enums.LineType;
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

public abstract class AbstractRouteStrategy implements RouteCalculationStrategy {

    protected final StationConnectionRepository connectionRepository;
    protected final StationRepository stationRepository;
    protected final LineFareFactory lineFareFactory;

    public AbstractRouteStrategy(StationConnectionRepository connectionRepository,
                                 StationRepository stationRepository,
                                 LineFareFactory lineFareFactory) {
        this.connectionRepository = connectionRepository;
        this.stationRepository = stationRepository;
        this.lineFareFactory = lineFareFactory;
    }

    // Ortak DTO Oluşturucu Metot (Eski RouteService'in kodları buraya taşındı)
    protected RouteResponseDto buildResponseDto(List<Long> routeIds) {
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
        int transferCount = 0;

        double totalFare = 0.0;
        double totalDuration = 0.0;
        double currentSegmentDuration = 0.0;

        for (int i = 0; i < routeIds.size() - 1; i++) {
            Long fromId = routeIds.get(i);
            Long toId = routeIds.get(i + 1);

            List<StationConnection> possibleConns = connectionRepository.findAll().stream()
                    .filter(c -> c.getStartStation().getId().equals(fromId) && c.getTargetStation().getId().equals(toId))
                    .collect(Collectors.toList());

            StationConnection selectedConn = null;

            if (currentLineId != null) {
                for (StationConnection c : possibleConns) {
                    if (c.getLine().getId().equals(currentLineId)) { selectedConn = c; break; }
                }
            }

            if (selectedConn == null && possibleConns.size() > 1 && i + 1 < routeIds.size() - 1) {
                Long nextToId = routeIds.get(i + 2);
                List<StationConnection> nextPossibleConns = connectionRepository.findAll().stream()
                        .filter(c -> c.getStartStation().getId().equals(toId) && c.getTargetStation().getId().equals(nextToId))
                        .collect(Collectors.toList());

                List<StationConnection> continuingConns = new ArrayList<>();
                for (StationConnection c : possibleConns) {
                    if (nextPossibleConns.stream().anyMatch(nc -> nc.getLine().getId().equals(c.getLine().getId()))) {
                        continuingConns.add(c);
                    }
                }
                if (!continuingConns.isEmpty()) {
                    selectedConn = continuingConns.stream().filter(c -> c.getLine().getType() != LineType.BANLIYO && c.getLine().getType() != LineType.METROBUS).findFirst().orElse(continuingConns.get(0));
                }
            }
            if (selectedConn == null) {
                selectedConn = possibleConns.stream().filter(c -> c.getLine().getType() != LineType.BANLIYO && c.getLine().getType() != LineType.METROBUS).findFirst().orElse(possibleConns.get(0));
            }

            Long edgeLineId = selectedConn.getLine().getId();
            String edgeLineName = selectedConn.getLine().getName();
            double travelTime = selectedConn.getDuration();

            if (currentLineId == null) {
                currentLineId = edgeLineId; currentLineName = edgeLineName; currentLineObj = selectedConn.getLine();
                currentStationCount = 1; currentSegmentDuration = travelTime;
            } else if (currentLineId.equals(edgeLineId)) {
                currentStationCount++; currentSegmentDuration += travelTime;
            } else {
                JourneySegmentDto segment = new JourneySegmentDto();
                segment.setLineId(currentLineId); segment.setLineName(currentLineName);
                segment.setStationCount(currentStationCount); segment.setSegmentDuration(currentSegmentDuration);

                FareStrategy fareStrategy = lineFareFactory.getFareStrategy(currentLineObj.getType());
                double segmentFare = fareStrategy.calculateFare(currentStationCount);

                if (!segments.isEmpty()) {
                    if (transferCount < 2) { segmentFare = segmentFare * 0.6; transferCount++; }
                    else { transferCount = 0; }
                    totalDuration += 5.0;
                }

                segment.setSegmentFare(segmentFare); totalFare += segmentFare; totalDuration += currentSegmentDuration;
                segments.add(segment);

                currentLineId = edgeLineId; currentLineName = edgeLineName; currentLineObj = selectedConn.getLine();
                currentStationCount = 1; currentSegmentDuration = travelTime;
            }
        }

        if (currentLineId != null) {
            JourneySegmentDto segment = new JourneySegmentDto();
            segment.setLineId(currentLineId); segment.setLineName(currentLineName);
            segment.setStationCount(currentStationCount); segment.setSegmentDuration(currentSegmentDuration);
            FareStrategy fareStrategy = lineFareFactory.getFareStrategy(currentLineObj.getType());
            double segmentFare = fareStrategy.calculateFare(currentStationCount);
            if (!segments.isEmpty()) {
                if (transferCount < 2) { segmentFare = segmentFare * 0.6; transferCount++; }
                else { transferCount = 0; }
                totalDuration += 5.0;
            }
            segment.setSegmentFare(segmentFare); totalFare += segmentFare; totalDuration += currentSegmentDuration;
            segments.add(segment);
        }

        RouteResponseDto response = new RouteResponseDto();
        response.setStations(orderedRoute);
        response.setSegments(segments);
        response.setTotalFare(totalFare);     // Varsayılan Hesaplama
        response.setTotalDuration(totalDuration); // Varsayılan Hesaplama
        return response;
    }
}