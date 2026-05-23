package project.istanbulrailroute.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import project.istanbulrailroute.applications.routing.RouteCalculationStrategy;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.infrastructure.jpa.StationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RouteService {

    // Spring, bu arayüzü uygulayan tüm sınıfları (M1, M2 gibi) bu liste içine otomatik doldurur!
    private final List<RouteCalculationStrategy> routingStrategies;
    private final StationRepository stationRepository;

    public RouteService(List<RouteCalculationStrategy> routingStrategies,
                        StationRepository stationRepository) {
        this.routingStrategies = routingStrategies;
        this.stationRepository = stationRepository;
    }

    public List<Station> getOptimalRoute(Long startId, Long endId, String preference) {

        // INTERFACE ÜZERİNDEN OTOMATİK SEÇİM:
        // Listeyi dön, her nesneye "Senin adın ne?" (getStrategyName) diye sor ve eşleşeni bul.
        RouteCalculationStrategy selectedStrategy = routingStrategies.stream()
                .filter(strategy -> strategy.getStrategyName().equalsIgnoreCase(preference))
                .findFirst()
                .orElseGet(() -> routingStrategies.stream()
                        .filter(s -> s.getStrategyName().equals("SHORTEST_TIME"))
                        .findFirst()
                        .orElse(routingStrategies.get(0))); // Eğer bulunamazsa varsayılan en kısa süreye dön

        System.out.println("[ROUTING] Interface Üzerinden Otomatik Seçilen Strateji: " + selectedStrategy.getClass().getSimpleName());

        // Seçilen stratejiyi çalıştırıyoruz
        List<Long> routeIds = selectedStrategy.calculateRoute(startId, endId);

        // İstasyon nesnelerini sıralı dizme mantığı (Aynen korunuyor)
        List<Station> unorderedStations = stationRepository.findAllById(routeIds);
        Map<Long, Station> stationMap = unorderedStations.stream()
                .collect(Collectors.toMap(Station::getId, Function.identity()));

        List<Station> orderedRoute = new ArrayList<>();
        for (Long id : routeIds) {
            orderedRoute.add(stationMap.get(id));
        }

        return orderedRoute;
    }
}
