package project.istanbulrailroute.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.istanbulrailroute.applications.RouteService;
import project.istanbulrailroute.domain.models.Station;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<List<Station>> calculateRoute(
            @RequestParam Long startId,
            @RequestParam Long endId,
            @RequestParam(defaultValue = "shortestTimeStrategy") String preference) { // Varsayılan: En Kısa Süre

        List<Station> route = routeService.getOptimalRoute(startId, endId, preference);
        return ResponseEntity.ok(route);
    }
}
