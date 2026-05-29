package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.istanbulrailroute.application.routing.RouteCalculationStrategy;
import project.istanbulrailroute.presentation.dto.routeDto.RouteResponseDto;

import java.util.List;

@Service
public class RouteService {

    private final List<RouteCalculationStrategy> routingStrategies;
    private final NotificationService notificationService;

    public RouteService(List<RouteCalculationStrategy> routingStrategies,
                        NotificationService notificationService) {
        this.routingStrategies = routingStrategies;
        this.notificationService = notificationService;
    }

    @Transactional
    public RouteResponseDto getOptimalRoute(Long startId, Long endId, String preference) {

        // 1. İstasyonlar kapalı mı diye kontrol et
        notificationService.checkStationAvailability(startId);
        notificationService.checkStationAvailability(endId);

        // 2. Kullanıcının seçtiği stratejiyi (IoC Listesinden) bul
        RouteCalculationStrategy selectedStrategy = routingStrategies.stream()
                .filter(strategy -> strategy.getStrategyName().equalsIgnoreCase(preference))
                .findFirst()
                .orElseGet(() -> routingStrategies.stream()
                        .filter(s -> s.getStrategyName().equals("SHORTEST_TIME"))
                        .findFirst()
                        .orElse(routingStrategies.get(0)));

        // 3. Stratejiyi çalıştır ve DTO'yu doğrudan Frontend'e (Controller'a) dön!
        return selectedStrategy.calculateRoute(startId, endId);
    }
}