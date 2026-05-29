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
    // Sadece bakım değil, tüm uyarıları tuttuğu için adını 'stationAlerts' olarak genelledik
    private final Map<Long, String> stationAlerts = new ConcurrentHashMap<>();

    public NotificationService(StationManagementService managementService, StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        managementService.registerObserver(this);
    }

    @PostConstruct
    public void init() {
        // Sistem kalkarken veritabanındaki tüm durakların son durumunu belleğe yükler
        stationRepository.findAll().forEach(this::updateAlertsForStation);
    }

    @Override
    public void onStationStatusChanged(Station station) {
        // Observer tetiklendiğinde (Admin panelinden durum değiştiğinde) anında çalışır
        updateAlertsForStation(station);
    }

    // DRY Prensibi: Kod tekrarını önlemek için ortak kontrol metodu yazıldı
    private void updateAlertsForStation(Station station) {
        StationStatus status = station.getStatus();
        String stationName = station.getName();

        if (status == StationStatus.MAINTENANCE) {
            stationAlerts.put(station.getId(), stationName + " station is temporarily closed due to maintenance.");
            System.out.println("NOTIFICATION: " + stationName + " is under maintenance.");

        } else if (status == StationStatus.DISRUPTED) {
            stationAlerts.put(station.getId(), stationName + " station is currently experiencing disruptions.");
            System.out.println("NOTIFICATION: " + stationName + " is disrupted.");

        } else if (status == StationStatus.UNDER_CONSTRUCTION) {
            stationAlerts.put(station.getId(), stationName + " station is under construction and not yet open.");
            System.out.println("NOTIFICATION: " + stationName + " is under construction.");

        } else if (status == StationStatus.DECOMMISSIONED) {
            stationAlerts.put(station.getId(), stationName + " station has been permanently decommissioned.");
            System.out.println("NOTIFICATION: " + stationName + " is decommissioned.");

        } else {
            // Durum OPERATIONAL veya ACTIVE ise uyarı listesinden çıkarılır
            stationAlerts.remove(station.getId());
            System.out.println("NOTIFICATION: " + stationName + " is operational.");
        }
    }

    public void checkStationAvailability(Long stationId) {
        if (stationAlerts.containsKey(stationId)) {
            throw new RuntimeException(stationAlerts.get(stationId));
        }
    }

    public List<String> getActiveAlerts() {
        return new ArrayList<>(stationAlerts.values());
    }
}