package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.infrastructure.StationConnectionRepository;
import project.istanbulrailroute.infrastructure.StationRepository;
import project.istanbulrailroute.presentation.dto.routeDto.StationResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final StationConnectionRepository connectionRepository;

    public StationService(StationRepository stationRepository, StationConnectionRepository connectionRepository) {
        this.stationRepository = stationRepository;
        this.connectionRepository = connectionRepository;
    }

    public List<StationResponse> getAllStations() {
        return stationRepository.findAll().stream()
                .map(station -> new StationResponse(station.getId(), station.getName(), station.getStatus()))
                .collect(Collectors.toList());
    }

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public Station updateStation(Long id, Station stationDetails) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));

        station.setName(stationDetails.getName());
        return stationRepository.save(station);
    }

    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));

        boolean hasActiveConnections = connectionRepository.existsByStartStationIdOrTargetStationId(id, id);

        if (hasActiveConnections) {
            throw new RuntimeException("Error: Cannot delete station. There are active connections linked to this station. Delete them first.");
        }

        stationRepository.delete(station);
    }
}