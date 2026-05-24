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
    private final LineService lineService;

    public FareController(FareService fareService, LineService lineService) {
        this.fareService = fareService;
        this.lineService = lineService;
    }

    @PostMapping("/pay")
    public ResponseEntity<Passenger> processPayment(
            @RequestParam Long passengerId,
            @RequestParam Long lineId,
            @RequestParam int stationCount) {

        Line line = lineService.getLineById(lineId);

        Passenger updatedPassenger = fareService.processFarePayment(passengerId, line, stationCount);

        return ResponseEntity.ok(updatedPassenger);
    }

    @PostMapping("/pay-journey")
    public ResponseEntity<Passenger> processJourneyPayment(@RequestBody PayJourneyRequest request) {
        Passenger updatedPassenger = fareService.processJourneyPayment(
                request.getPassengerId(),
                request.getSegments()
        );
        return ResponseEntity.ok(updatedPassenger);
    }
}