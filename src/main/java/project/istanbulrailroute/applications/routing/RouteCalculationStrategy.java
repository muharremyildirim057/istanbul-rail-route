package project.istanbulrailroute.applications.routing;

import java.util.List;

public interface RouteCalculationStrategy {
    List<Long> calculateRoute(Long startStationId, Long endStationId);
    String getStrategyName();
}
