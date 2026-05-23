package project.istanbulrailroute.applications.routing.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.istanbulrailroute.applications.routing.RouteCalculationStrategy;
import project.istanbulrailroute.infrastructure.jpa.StationRepository;

import java.util.List;

@Service("leastTransferStrategy")
public class LeastTransferStrategy implements RouteCalculationStrategy {

    private final StationRepository stationRepository;

    public LeastTransferStrategy(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }


    @Override
    public List<Long> calculateRoute(Long startStationId, Long endStationId) {
        List<Long> routeIds = stationRepository.findLeastTransferPathIds(startStationId, endStationId);

        if (routeIds == null || routeIds.isEmpty()) {
            throw new RuntimeException("Hedef istasyona ulaşılamıyor veya rota bulunamadı!");
        }
        return routeIds;
    }

    @Override
    public String getStrategyName() {
        return "LEAST_TRANSFER";
    }

}
