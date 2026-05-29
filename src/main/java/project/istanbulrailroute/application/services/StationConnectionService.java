package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.domain.models.StationConnection;
import project.istanbulrailroute.infrastructure.LineRepository;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.infrastructure.StationRepository;
import project.istanbulrailroute.presentation.dto.routeDto.ConnectionRequestDto;
import project.istanbulrailroute.presentation.dto.routeDto.ConnectionResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StationConnectionService {

    private final StationConnectionRepository connectionRepository;
    private final StationRepository stationRepository;
    private final LineRepository lineRepository;

    public StationConnectionService(StationConnectionRepository connectionRepository,
                                    StationRepository stationRepository,
                                    LineRepository lineRepository) {
        this.connectionRepository = connectionRepository;
        this.stationRepository = stationRepository;
        this.lineRepository = lineRepository;
    }

    @Transactional
    public void createBidirectionalConnection(ConnectionRequestDto request) {

        boolean exists = connectionRepository.existsByStartStationIdAndTargetStationIdAndLineId(
                request.getStartStationId(),
                request.getTargetStationId(),
                request.getLineId()
        );

        if (exists) {
            throw new RuntimeException("Error: A connection already exists between these stations for the selected line!");
        }

        Station start = stationRepository.findById(request.getStartStationId())
                .orElseThrow(() -> new RuntimeException("Start station not found."));

        Station target = stationRepository.findById(request.getTargetStationId())
                .orElseThrow(() -> new RuntimeException("Target station not found."));

        Line line = lineRepository.findById(request.getLineId())
                .orElseThrow(() -> new RuntimeException("Line not found."));

        StationConnection forwardConnection = new StationConnection();
        forwardConnection.setStartStation(start);
        forwardConnection.setTargetStation(target);
        forwardConnection.setLine(line);
        forwardConnection.setDuration(request.getDuration());
        connectionRepository.save(forwardConnection);

        StationConnection backwardConnection = new StationConnection();
        backwardConnection.setStartStation(target);
        backwardConnection.setTargetStation(start);
        backwardConnection.setLine(line);
        backwardConnection.setDuration(request.getDuration());
        connectionRepository.save(backwardConnection);
    }

    public List<ConnectionResponseDto> getAllConnections() {
        List<StationConnection> connections = connectionRepository.findAll();

        return connections.stream().map(c -> new ConnectionResponseDto(
                c.getId(),
                c.getStartStation() != null ? c.getStartStation().getName() : "N/A",
                c.getTargetStation() != null ? c.getTargetStation().getName() : "N/A",
                c.getLine() != null ? c.getLine().getName() : "N/A",
                c.getDuration()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void deleteConnection(Long id) {
        StationConnection conn = connectionRepository.findById(id).orElse(null);
        if (conn != null) {
            // 1. Orijinal bağlantının "Tam Tersini" (B -> A) veritabanında ara ve sil
            connectionRepository.findFirstByStartStationIdAndTargetStationId(
                    conn.getTargetStation().getId(),
                    conn.getStartStation().getId()
            ).ifPresent(reverseConn -> {
                // Sadece aynı hatta ait ters bağlantıyı sildiğimizden emin oluyoruz
                if (reverseConn.getLine().getId().equals(conn.getLine().getId())) {
                    connectionRepository.delete(reverseConn);
                }
            });

            // 2. Tıklanan orijinal bağlantıyı (A -> B) sil
            connectionRepository.delete(conn);
        }
    }
}