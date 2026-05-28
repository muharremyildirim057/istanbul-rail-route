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
    public Passenger processFarePayment(Long passengerId, Line line, int stationCount) {
        Passenger passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found!"));

        FareStrategy fareStrategy = lineFareFactory.getFareStrategy(line.getType());

        double finalFare = fareStrategy.calculateFare(stationCount);

        boolean paymentSuccess = passenger.getVirtualCard().payFare(finalFare, line.getId());

        if (!paymentSuccess) {
            throw new RuntimeException("Insufficient balance! Journey fare: " + finalFare + " TRY. Current balance: " + passenger.getVirtualCard().getBalance() + " TRY");
        }

        return userRepository.save(passenger);
    }

    @Transactional
    public Passenger processJourneyPayment(Long passengerId, List<JourneySegmentDto> segments) {
        Passenger passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found!"));

        for (JourneySegmentDto segment : segments) {
            Line line = lineRepository.findById(segment.getLineId())
                    .orElseThrow(() -> new IllegalArgumentException("Line not found!"));

            FareStrategy fareStrategy = lineFareFactory.getFareStrategy(line.getType());

            double baseFare = fareStrategy.calculateFare(segment.getStationCount());

            boolean paymentSuccess = passenger.getVirtualCard().payFare(baseFare, line.getId());

            if (!paymentSuccess) {
                throw new RuntimeException("Insufficient balance! Not enough balance for line " + line.getName() + " during transfer.");
            }
        }

        return userRepository.save(passenger);
    }
}