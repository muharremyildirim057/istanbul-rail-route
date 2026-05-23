package project.istanbulrailroute.applications.routing.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.istanbulrailroute.applications.routing.RouteCalculationStrategy;
import project.istanbulrailroute.infrastructure.jpa.StationRepository;

import java.util.List;

@Service("shortestTimeStrategy")
public class ShortestTimeRouteStrategy implements RouteCalculationStrategy {
    @Autowired
    private final StationRepository stationRepository;


    public ShortestTimeRouteStrategy(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public List<Long> calculateRoute(Long startStationId, Long endStationId) {
        List<Long> routeIds = stationRepository.findShortestPathIds(startStationId,endStationId);

        if(routeIds == null || routeIds.isEmpty()){
            throw new RuntimeException("Hedef istasyona ulaşılamıyor veya rota bulunamadı!");
        }
        return routeIds;
    }

    @Override
    public String getStrategyName() {
        return "SHORTEST_TIME"; // Büyük harf standart bir kuraldır
    }
}
