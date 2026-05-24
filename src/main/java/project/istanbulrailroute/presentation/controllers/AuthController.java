package project.istanbulrailroute.presentation.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import project.istanbulrailroute.application.services.AuthService;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.presentation.dto.authDto.LoginRequest;
import project.istanbulrailroute.presentation.dto.authDto.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    // Spring IoC Container tarafından yönetilen Singleton enjeksiyonu
    public AuthController(AuthService authService, ResourceUrlProvider resourceUrlProvider) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Passenger> loginPassenger(@RequestBody LoginRequest request) {
        // Tek satır! Hata olursa otomatik olarak 401 ve List<String> fırlayacak.
        Passenger passenger = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok().body(passenger);
    }

    @PostMapping("/register")
    public ResponseEntity<Passenger> registerPassenger(@Valid @RequestBody RegisterRequest request) {
        Passenger savedPassenger = authService.registerPassenger(
                request.getUsername(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPassenger);
    }

    @GetMapping("/me")
    public ResponseEntity<Passenger> getCurrentPassenger(@RequestParam String username) {
        Passenger passenger = authService.getPassengerProfile(username);
        return ResponseEntity.ok().body(passenger);
    }
}