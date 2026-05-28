package project.istanbulrailroute.application.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.istanbulrailroute.domain.exception.passengerException.UserNotFoundException;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.exception.passengerException.InvalidCredentialsException;
import project.istanbulrailroute.domain.exception.passengerException.UserAlreadyExistsException;
import project.istanbulrailroute.infrastructure.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Passenger login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(passenger -> passwordEncoder.matches(password, passenger.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
    }

    @Transactional
    public Passenger registerPassenger(String username, String password, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("This username is already in use!");
        }

        String rawPassword = password;
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Passenger newPassenger = new Passenger();
        newPassenger.setUsername(username);
        newPassenger.setPasswordHash(encodedPassword);
        newPassenger.setFirstName(firstName);
        newPassenger.setLastName(lastName);
        newPassenger.setRole("PASSENGER");

        project.istanbulrailroute.domain.models.VirtualCard newCard = new project.istanbulrailroute.domain.models.VirtualCard();
        newCard.setBalance(0.0);
        newPassenger.setVirtualCard(newCard);

        return userRepository.save(newPassenger);
    }

    public Passenger getPassengerProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Unauthorized: User not found"));
    }
}