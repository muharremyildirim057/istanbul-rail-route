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
    // Bakımda olan istasyonları hızlıca bulmak için RAM'de tutuyoruz
    private final Map<Long, String> maintenanceAlerts = new ConcurrentHashMap<>();

    public NotificationService(StationManagementService managementService, StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        // Servis başlarken kendini Subject'e (StationManagementService) kayıt eder
        managementService.registerObserver(this);
    }

    @PostConstruct
    public void init() {
        // Sunucu ilk çalıştığında veritabanındaki mevcut bakım durumlarını hafızaya alır
        stationRepository.findAll().forEach(station -> {
            if (station.getStatus() == StationStatus.MAINTENANCE) {
                maintenanceAlerts.put(station.getId(), station.getName() + " istasyonu bakım çalışması nedeniyle geçici olarak kapalıdır.");
            } else if (station.getStatus() == StationStatus.CLOSED) {
                maintenanceAlerts.put(station.getId(),station.getName() + " istasyonu geçiçi olarak kapalıdır.");
            }
        });
    }

    @Override
    public void onStationStatusChanged(Station station) {
        // Admin durumu değiştirdiğinde anında tetiklenir
        if (station.getStatus() == StationStatus.MAINTENANCE) {
            maintenanceAlerts.put(station.getId(), station.getName() + " istasyonu bakım çalışması nedeniyle geçici olarak kapalıdır.");
            System.out.println("BİLDİRİM: " + station.getName() + " bakıma alındı.");
        } else {
            maintenanceAlerts.remove(station.getId());
            System.out.println("BİLDİRİM: " + station.getName() + " tekrar aktif edildi.");
        }
    }

    // Rota hesaplanırken durağın uygun olup olmadığını kontrol eder
    public void checkStationAvailability(Long stationId) {
        if (maintenanceAlerts.containsKey(stationId)) {
            // Eğer durak bakımdaysa, doğrudan hata fırlat (Ön yüz bunu msgBox'ta gösterecek)
            throw new RuntimeException(maintenanceAlerts.get(stationId));
        }
    }

    // İstenirse ön yüze aktif uyarıları bir liste olarak dönmek için kullanılabilir
    public List<String> getActiveAlerts() {
        return new ArrayList<>(maintenanceAlerts.values());
    }
}
