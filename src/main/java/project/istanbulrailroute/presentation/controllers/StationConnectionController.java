package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.application.services.StationConnectionService;
import project.istanbulrailroute.presentation.dto.routeDto.ConnectionRequestDto;
import project.istanbulrailroute.presentation.dto.routeDto.ConnectionResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/stations/connections")
public class StationConnectionController {

    private final StationConnectionService connectionService;

    public StationConnectionController(StationConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping
    public ResponseEntity<String> createConnection(@RequestBody ConnectionRequestDto request) {
        connectionService.createBidirectionalConnection(request);
        return ResponseEntity.ok("Stations have been successfully connected bidirectionally!");
    }

    @GetMapping
    public ResponseEntity<List<ConnectionResponseDto>> getAllConnections() {
        List<ConnectionResponseDto> dtos = connectionService.getAllConnections();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(@PathVariable Long id) {
        connectionService.deleteConnection(id);
        return ResponseEntity.noContent().build();
    }
}