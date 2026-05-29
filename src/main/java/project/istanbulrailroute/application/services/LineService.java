package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.infrastructure.LineRepository;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.presentation.dto.routeDto.LineAdminResponseDto;
import project.istanbulrailroute.presentation.dto.routeDto.LineDetailedDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineService {

    private final LineRepository lineRepository;
    private final StationConnectionRepository connectionRepository;

    public LineService(LineRepository lineRepository, StationConnectionRepository connectionRepository) {
        this.lineRepository = lineRepository;
        this.connectionRepository = connectionRepository;
    }

    public List<LineAdminResponseDto> getAllLines() {
        return lineRepository.findAll().stream()
                .map(line -> new LineAdminResponseDto(
                        line.getId(),
                        line.getName(),
                        line.getType() != null ? line.getType().toString() : "N/A"
                ))
                .collect(Collectors.toList());
    }

    public Line createLine(Line line) {
        return lineRepository.save(line);
    }

    public Line updateLine(Long id, Line lineDetails) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Line not found with id: " + id));

        line.setName(lineDetails.getName());
        line.setType(lineDetails.getType());

        return lineRepository.save(line);
    }

    public void deleteLine(Long id) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Line not found with id: " + id));

        boolean hasActiveConnections = connectionRepository.existsByLineId(id);

        if (hasActiveConnections) {
            throw new RuntimeException("Error: Cannot delete line. There are active connections linked to this line. Delete them first.");
        }

        lineRepository.delete(line);
    }

    public List<LineDetailedDto> getLinesDetailed() {
        List<Line> lines = lineRepository.findAll();

        return lines.stream().map(line -> {
            List<String> stations = connectionRepository.findAll().stream()
                    .filter(conn -> conn.getLine().getId().equals(line.getId()))
                    .flatMap(conn -> java.util.stream.Stream.of(conn.getStartStation().getName(), conn.getTargetStation().getName()))
                    .distinct()
                    .collect(Collectors.toList());

            return new LineDetailedDto(
                    line.getId(),
                    line.getName(),
                    line.getType() != null ? line.getType().toString() : "N/A",
                    stations
            );
        }).collect(Collectors.toList());
    }

    public Line getLineById(Long id) {
        return lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid line selection! Line not found."));
    }
}