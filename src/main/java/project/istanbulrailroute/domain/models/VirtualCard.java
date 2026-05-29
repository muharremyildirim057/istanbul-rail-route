package project.istanbulrailroute.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_cards")
@Data
@NoArgsConstructor
public class VirtualCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double balance;

    @Column(name = "last_line_id")
    private Long lastLineId;

    @Column(name = "last_used_at")
    private LocalDateTime firstBoardingTime;

    @Column(name = "transfer_count")
    private int transferCount = 0;

    public void topUp(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    // VirtualCard.java içine bu metodu ekle
    public boolean payDirect(double totalAmount) {
        // Küsürat hatalarını önlemek için yuvarlama (örn: 9.9999 -> 10.00)
        double finalAmount = Math.round(totalAmount * 100.0) / 100.0;
        double currentBalance = Math.round(this.balance * 100.0) / 100.0;

        if (currentBalance >= finalAmount) {
            this.balance -= finalAmount;
            return true;
        }
        return false;
    }
}
