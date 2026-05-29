package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.application.services.FareService;
import project.istanbulrailroute.application.services.LineService;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.presentation.dto.paymentDto.PayJourneyRequest;

@RestController
@RequestMapping("/api/fare")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService, LineService lineService) {
        this.fareService = fareService;

    }


    @PostMapping("/pay-journey")
    public ResponseEntity<Passenger> processJourneyPayment(@RequestBody PayJourneyRequest request) {
        // Artık segments yerine totalFare gönderiyoruz
        Passenger updatedPassenger = fareService.processJourneyPayment(
                request.getPassengerId(),
                request.getTotalFare()
        );
        return ResponseEntity.ok(updatedPassenger);
    }
}