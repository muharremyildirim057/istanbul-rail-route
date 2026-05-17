package project.istanbulrailroute.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public void topUp(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean payFare(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true; // Geçiş onaylandı
        }
        return false; // Yetersiz bakiye
    }
}
