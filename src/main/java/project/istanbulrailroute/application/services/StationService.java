package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.infrastructure.StationRepository;
import project.istanbulrailroute.presentation.dto.routeDto.StationResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository stationRepository;

    // Dependency Injection (Constructor Injection)
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationResponse> getAllStations() {
        return stationRepository.findAll().stream()
                .map(station -> new StationResponse(station.getId(), station.getName(), station.getStatus()))
                .collect(Collectors.toList());
    }

    public Station createStation(Station station) {
        // İleride buraya "Aynı isimde istasyon var mı?" gibi iş kuralları (business logic) eklenebilir.
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

        stationRepository.delete(station);
    }
}
