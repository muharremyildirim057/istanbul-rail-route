package project.istanbulrailroute.applications;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.exception.passengerException.InvalidCredentialsException;
import project.istanbulrailroute.domain.exception.passengerException.UserAlreadyExistsException;
import project.istanbulrailroute.infrastructure.jpa.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    // Singleton yönetimi için Constructor Injection
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Passenger login(String username,String password){
        return userRepository.findByUsername(username)
                .filter(passenger -> passenger.getPasswordHash().equals(password))
                .orElseThrow(() -> new InvalidCredentialsException("Geçersiz kullanıcı adı veya şifre"));
    }



    @Transactional
    public Passenger registerPassenger(String username, String password, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Bu kullanıcı adı zaten kullanımda!");
        }

        Passenger newPassenger = new Passenger();
        newPassenger.setUsername(username);
        newPassenger.setPasswordHash(password);
        newPassenger.setFirstName(firstName); // Eklendi
        newPassenger.setLastName(lastName);   // Eklendi
        newPassenger.setRole("PASSENGER");

        // Sanal kart 0 TL olarak oluşturulur [cite: 1076]
        project.istanbulrailroute.domain.models.VirtualCard newCard = new project.istanbulrailroute.domain.models.VirtualCard();
        newCard.setBalance(0.0);
        newPassenger.setVirtualCard(newCard);

        return userRepository.save(newPassenger);
    }
}
