package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.application.services.StationService;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.presentation.dto.routeDto.StationResponse;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    // Artık Repository değil, Service enjekte ediyoruz
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {
        List<StationResponse> stations = stationService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    @PostMapping
    public ResponseEntity<Station> createStation(@RequestBody Station station) {
        Station savedStation = stationService.createStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable Long id, @RequestBody Station stationDetails) {
        Station updatedStation = stationService.updateStation(id, stationDetails);
        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}