package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.application.services.LineService;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.presentation.dto.routeDto.LineAdminResponseDto;
import project.istanbulrailroute.presentation.dto.routeDto.LineDetailedDto;

import java.util.List;

@RestController
@RequestMapping("/api/lines")
public class LineController {

    private final LineService lineService;

    public LineController(LineService lineService) {
        this.lineService = lineService;
    }

    @GetMapping
    public ResponseEntity<List<LineAdminResponseDto>> getAllLines() {
        return ResponseEntity.ok(lineService.getAllLines());
    }

    @PostMapping
    public ResponseEntity<Line> createLine(@RequestBody Line line) {
        Line savedLine = lineService.createLine(line);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLine);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Line> updateLine(@PathVariable Long id, @RequestBody Line lineDetails) {
        Line updatedLine = lineService.updateLine(id, lineDetails);
        return ResponseEntity.ok(updatedLine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLine(@PathVariable Long id) {
        lineService.deleteLine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/details")
    public ResponseEntity<List<LineDetailedDto>> getLinesDetailed() {
        return ResponseEntity.ok(lineService.getLinesDetailed());
    }
}