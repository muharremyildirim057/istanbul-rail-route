package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.application.services.StationManagementService;
import project.istanbulrailroute.domain.models.Station;
import project.istanbulrailroute.domain.models.enums.StationStatus;

@RestController
@RequestMapping("/api/admin/stations")
public class StationAdminController {

    private final StationManagementService stationManagementService;

    public StationAdminController(StationManagementService stationManagementService) {
        this.stationManagementService = stationManagementService;
    }

    // İstasyon durumunu güncelleme Endpoint'i (Örn: PUT /api/admin/stations/1/status?newStatus=MAINTENANCE)
    @PutMapping("/{id}/status")
    public ResponseEntity<Station> updateStatus(
            @PathVariable Long id,
            @RequestParam StationStatus newStatus) {

        Station updatedStation = stationManagementService.updateStationStatus(id, newStatus);
        return ResponseEntity.ok(updatedStation);
    }
}
