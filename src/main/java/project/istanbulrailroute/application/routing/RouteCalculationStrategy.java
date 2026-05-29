package project.istanbulrailroute.application.routing;

import project.istanbulrailroute.presentation.dto.routeDto.RouteResponseDto;

import java.util.List;

public interface RouteCalculationStrategy {
    RouteResponseDto calculateRoute(Long startStationId, Long endStationId);
    String getStrategyName();
}
