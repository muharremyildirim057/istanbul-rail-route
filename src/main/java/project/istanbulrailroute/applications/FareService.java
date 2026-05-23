package project.istanbulrailroute.applications;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import project.istanbulrailroute.applications.fare.FareStrategy;
import project.istanbulrailroute.applications.fare.LineFareFactory;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.models.Line;
import project.istanbulrailroute.infrastructure.jpa.UserRepository;

@Service
public class FareService {

    private final LineFareFactory lineFareFactory;
    private final UserRepository userRepository;

    // Bağımlılıklar Constructor Injection (Singleton) ile enjekte ediliyor
    public FareService(LineFareFactory lineFareFactory, UserRepository userRepository) {
        this.lineFareFactory = lineFareFactory;
        this.userRepository = userRepository;
    }

    /**
     * Yolcunun yaptığı seyahate göre kartından ücret tahsil eder.
     * @param passengerId Seyahat eden yolcunun ID'si
     * @param line Seyahat edilen hat (İçindeki LineType enumu fabrikaya gönderilecek)
     * @param stationCount Kaç durak gidildiği
     * @return Güncellenmiş bakiye bilgisi ile Yolcu nesnesi
     */
    @Transactional
    public Passenger processFarePayment(Long passengerId, Line line, int stationCount) {
        // 1. Yolcuyu veritabanından bul
        Passenger passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Yolcu bulunamadı!"));

        // 2. Factory Pattern kullanarak hattan uygun ücretlendirme stratejisini al
        FareStrategy fareStrategy = lineFareFactory.getFareStrategy(line.getType());

        // 3. Strateji üzerinden dinamik olarak ücreti hesapla
        double finalFare = fareStrategy.calculateFare(stationCount);

        // 4. Yolcunun sanal kartından (VirtualCard) ücreti düşmeyi dene
        boolean paymentSuccess = passenger.getVirtualCard().payFare(finalFare, line.getId());

        if (!paymentSuccess) {
            // İleride buraya özel bir InsufficientBalanceException yazabiliriz, şimdilik runtime fırlatıyoruz
            throw new RuntimeException("Yetersiz bakiye! Seyahat ücreti: " + finalFare + " TL. Mevcut bakiyeniz: " + passenger.getVirtualCard().getBalance() + " TL");
        }

        System.out.println("[SİMÜLASYON] Ödeme Onaylandı. Yolcu: " + passenger.getUsername() +
                " | Hat: " + line.getName() + " (" + line.getType() + ")" +
                " | Durak Sayısı: " + stationCount + " | Kesilen Ücret: " + finalFare + " TL");

        // 5. Güncellenmiş yolcu ve kart bilgisini veritabanına kaydet ve dön
        return userRepository.save(passenger);
    }
}