package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.management.StationObserver;
import project.istanbulrailroute.application.management.StationSubject;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.domain.models.enums.StationStatus;
import project.istanbulrailroute.infrastructure.StationRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class StationManagementService implements StationSubject {

    private final StationRepository stationRepository;
    private final List<StationObserver> observers = new ArrayList<>();

    public StationManagementService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station updateStationStatus(Long id, StationStatus newStatus) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found. ID: " + id));

        station.setStatus(newStatus);
        Station updatedStation = stationRepository.save(station);

        notifyObservers(updatedStation);

        return updatedStation;
    }

    @Override
    public void registerObserver(StationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(StationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Station station) {
        for (StationObserver observer : observers) {
            observer.onStationStatusChanged(station);
        }
    }
}