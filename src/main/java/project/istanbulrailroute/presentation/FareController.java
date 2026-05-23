package project.istanbulrailroute.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.istanbulrailroute.applications.FareService;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.infrastructure.jpa.LineRepository;
import project.istanbulrailroute.infrastructure.jpa.StationRepository;

@RestController
@RequestMapping("/api/fare")
public class FareController {

    private final FareService fareService;
    private final LineRepository lineRepository; // Hattı doğrulamak ve çekmek için enjekte ediyoruz

    // Constructor Injection (Singleton)
    public FareController(FareService fareService, LineRepository lineRepository) {
        this.fareService = fareService;
        this.lineRepository = lineRepository;
    }

    /**
     * Yolcu seyahat biletini sanal kartından tahsil eder.
     * Postman Testi için HTTP POST isteği oluşturur.
     */
    @PostMapping("/pay")
    public ResponseEntity<Passenger> processPayment(
            @RequestParam Long passengerId,
            @RequestParam Long lineId,
            @RequestParam int stationCount) {

        // Veritabanından yolculuk yapılan gerçek hattı buluyoruz (Türü öğrenmek için: METRO, BANLIYO vb.)
        Line line = lineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Hatalı hat seçimi! Hat bulunamadı."));

        // Ücretlendirme servisini tetikliyoruz
        Passenger updatedPassenger = fareService.processFarePayment(passengerId, line, stationCount);

        return ResponseEntity.ok(updatedPassenger);
    }
}