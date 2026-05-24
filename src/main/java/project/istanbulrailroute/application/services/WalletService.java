package project.istanbulrailroute.application.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.domain.exception.virtualCardException.InvalidCardException;
import project.istanbulrailroute.infrastructure.UserRepository;

@Service
public class WalletService {
    private final UserRepository userRepository;

    public WalletService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Transactional
    public Passenger topUpBalance(Long passengerId, String creditCardNumber, double amount){
        if(amount < 30){
            throw new InvalidCardException("Yükleme başarısız! Tek seferde en az 30 TL yükleyebilirsiniz.");
        }if (amount > 3500) {
            throw new InvalidCardException("Yükleme başarısız! Tek seferde en fazla 3500 TL yükleyebilirsiniz.");
        }

        if (!validateLuhn(creditCardNumber)) {
            throw new InvalidCardException("Geçersiz kredi kartı numarası! [Luhn Doğrulaması Başarısız]");
        }
        // 3. Yolcuyu veritabanından bulma
        Passenger passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Yolcu bulunamadı!"));

        // 4. Maksimum Toplam Bakiye Kontrolü (Toplam bakiye 5000 TL'yi aşamaz)
        double currentBalance = passenger.getVirtualCard().getBalance();
        if (currentBalance + amount > 5000) {
            throw new InvalidCardException("Yükleme başarısız! Kart toplam bakiyesi 5000 TL sınırını aşamaz. Mevcut bakiyeniz: " + currentBalance + " TL");
        }

        // 5. Her şey yolundaysa domain modelimizdeki iş kuralını tetikleme (Para yükleme)
        passenger.getVirtualCard().topUp(amount);

        System.out.println("[SIMÜLASYON] Limitler onaylandı. Sanal karta " + amount + " TL yüklendi.");
        return userRepository.save(passenger);
    }


    private boolean validateLuhn(String cardNumber) {
        if (cardNumber == null) return false;

        String digitsOnly = cardNumber.replaceAll("\\s+", "");
        if (digitsOnly.length() < 16 || digitsOnly.length() > 16) return false;

        int sum = 0;
        boolean alternate = false;

        for (int i = digitsOnly.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(digitsOnly.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

}
