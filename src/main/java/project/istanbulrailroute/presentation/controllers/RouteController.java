package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.istanbulrailroute.application.services.RouteService;
import project.istanbulrailroute.presentation.dto.routeDto.RouteResponseDto;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<RouteResponseDto> calculateRoute(
            @RequestParam Long startId,
            @RequestParam Long endId,
            @RequestParam(defaultValue = "SHORTEST_TIME") String preference) {

        RouteResponseDto response = routeService.getOptimalRoute(startId, endId, preference);
        return ResponseEntity.ok(response);
    }
}
