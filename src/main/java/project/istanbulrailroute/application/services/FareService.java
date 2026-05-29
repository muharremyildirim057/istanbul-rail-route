package project.istanbulrailroute.application.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import project.istanbulrailroute.application.fare.FareStrategy;
import project.istanbulrailroute.application.fare.LineFareFactory;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.infrastructure.LineRepository;
import project.istanbulrailroute.infrastructure.UserRepository;
import project.istanbulrailroute.presentation.dto.journeyDto.JourneySegmentDto;

import java.util.List;

@Service
public class FareService {

    private final LineFareFactory lineFareFactory;
    private final UserRepository userRepository;
    private final LineRepository lineRepository;

    public FareService(LineFareFactory lineFareFactory, UserRepository userRepository, LineRepository lineRepository) {
        this.lineFareFactory = lineFareFactory;
        this.userRepository = userRepository;
        this.lineRepository = lineRepository;
    }

    @Transactional
    public Passenger processJourneyPayment(Long passengerId, double totalFare) {
        Passenger passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found!"));

        // Hesaplamalar zaten yapıldığı için direkt net tutarı düşüyoruz
        boolean paymentSuccess = passenger.getVirtualCard().payDirect(totalFare);

        if (!paymentSuccess) {
            throw new RuntimeException("Insufficient balance! Total journey fare: " + totalFare + " TRY.");
        }

        return userRepository.save(passenger);
    }
}