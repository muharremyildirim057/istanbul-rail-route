package project.istanbulrailroute.applications.routing.impl;

import project.istanbulrailroute.applications.routing.RouteCalculationStrategy;
import project.istanbulrailroute.infrastructure.jpa.StationRepository;

import java.util.List;

public class LeastStopsRouteStrategy implements RouteCalculationStrategy {
    private final StationRepository stationRepository;

    public LeastStopsRouteStrategy(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public List<Long> calculateRoute(Long startStationId, Long endStationId) {
        List<Long> routeIds = stationRepository.findLeastStopsPathIds(startStationId, endStationId);

        if (routeIds == null || routeIds.isEmpty()) {
            throw new RuntimeException("Hedef istasyona ulaşılamıyor veya rota bulunamadı!");
        }
        return routeIds;
    }

    // İŞTE YENİ KİMLİĞİMİZ!
    @Override
    public String getStrategyName() {
        return "LEAST_STOPS";
    }
}
