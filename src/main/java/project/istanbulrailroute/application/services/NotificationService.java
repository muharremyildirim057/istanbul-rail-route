package project.istanbulrailroute.application.services;

import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.management.StationObserver;
import project.istanbulrailroute.domain.models.Station;

import jakarta.annotation.PostConstruct;
import project.istanbulrailroute.domain.models.enums.StationStatus;
import project.istanbulrailroute.infrastructure.StationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService implements StationObserver {

    private final StationRepository stationRepository;
    private final Map<Long, String> maintenanceAlerts = new ConcurrentHashMap<>();

    public NotificationService(StationManagementService managementService, StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        managementService.registerObserver(this);
    }

    @PostConstruct
    public void init() {
        stationRepository.findAll().forEach(station -> {
            if (station.getStatus() == StationStatus.MAINTENANCE) {
                maintenanceAlerts.put(station.getId(), station.getName() + " station is temporarily closed due to maintenance.");
            } else if (station.getStatus() == StationStatus.CLOSED) {
                maintenanceAlerts.put(station.getId(),station.getName() + " station is temporarily closed.");
            }
        });
    }

    @Override
    public void onStationStatusChanged(Station station) {
        if (station.getStatus() == StationStatus.MAINTENANCE) {
            maintenanceAlerts.put(station.getId(), station.getName() + " station is temporarily closed due to maintenance.");
            System.out.println("NOTIFICATION: " + station.getName() + " is under maintenance.");
        }else if(station.getStatus() == StationStatus.CLOSED){
            maintenanceAlerts.put(station.getId(), station.getName() + " station is temporarily closed.");
            System.out.println("NOTIFICATION: " + station.getName() + " is closed.");
        }
        else {
            maintenanceAlerts.remove(station.getId());
            System.out.println("NOTIFICATION: " + station.getName() + " is active again.");
        }
    }

    public void checkStationAvailability(Long stationId) {
        if (maintenanceAlerts.containsKey(stationId)) {
            throw new RuntimeException(maintenanceAlerts.get(stationId));
        }
    }

    public List<String> getActiveAlerts() {
        return new ArrayList<>(maintenanceAlerts.values());
    }
}