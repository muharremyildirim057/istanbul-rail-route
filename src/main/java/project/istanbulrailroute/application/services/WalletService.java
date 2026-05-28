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
            throw new InvalidCardException("Top-up failed! You must top up at least 30 TRY at a time.");
        }if (amount > 3500) {
            throw new InvalidCardException("Top-up failed! You can top up a maximum of 3500 TRY at a time.");
        }

        if (!validateLuhn(creditCardNumber)) {
            throw new InvalidCardException("Invalid credit card number! [Luhn Validation Failed]");
        }

        Passenger passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found!"));

        double currentBalance = passenger.getVirtualCard().getBalance();
        if (currentBalance + amount > 5000) {
            throw new InvalidCardException("Top-up failed! Card total balance cannot exceed 5000 TRY limit. Current balance: " + currentBalance + " TRY");
        }

        passenger.getVirtualCard().topUp(amount);

        System.out.println("[SIMULATION] Limits approved. " + amount + " TRY loaded to the virtual card.");
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