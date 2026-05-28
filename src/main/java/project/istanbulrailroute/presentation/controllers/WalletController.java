package project.istanbulrailroute.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.application.services.WalletService;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.presentation.dto.paymentDto.TopUpRequest;

@RestController
@RequestMapping("/api/payment")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{id}/topup")
    public ResponseEntity<Passenger> topUp(@PathVariable Long id, @RequestBody TopUpRequest request){
        Passenger updatedPassenger = walletService.topUpBalance(
                id,
                request.getCreditCardNumber(),
                request.getAmount()
        );
        return ResponseEntity.ok(updatedPassenger);
    }
}