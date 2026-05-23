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

    public boolean payFare(double baseAmount, Long currentLineId) {
        double finalAmount = baseAmount;
        LocalDateTime now = LocalDateTime.now();
        boolean isTransferValid = false;

        // 1. KONTROL: 2 saatlik (120 dk) aktarma penceresi iç  inde miyiz?
        if (firstBoardingTime != null && Duration.between(firstBoardingTime, now).toMinutes() <= 120) {

            // 2. KONTROL: Farklı bir hat mı? VE Aktarma limiti (Max 2) dolmamış mı?
            if (lastLineId != null && !lastLineId.equals(currentLineId) && transferCount < 2) {

                // Kriterler sağlandı! %40 indirim uygula
                finalAmount = baseAmount * 0.60;
                isTransferValid = true;
            }
        }

        // Bakiye Kontrolü ve Tahsilat
        if (this.balance >= finalAmount) {
            this.balance -= finalAmount; // Parayı düş

            // DURUM GÜNCELLEMESİ (Zinciri devam ettir veya sıfırla)
            if (isTransferValid) {
                // Aktarma yapıldı: Sadece aktarma sayacını ve son hattı güncelle.
                // firstBoardingTime DEĞİŞMEZ! (Çünkü 2 saatlik pencere ilk basımdan başlar)
                this.transferCount++;
                this.lastLineId = currentLineId;
                System.out.println("[BİLET] Aktarma Başarılı (" + this.transferCount + "/2). İndirimli Ücret: " + finalAmount + " TL");
            } else {
                // Aktarma DEĞİLSE (Süre dolduysa, aynı hatsa veya 2 limitini aştıysa)
                // Yeni bir 120 dakikalık zincir (Pencere) başlatıyoruz!
                this.firstBoardingTime = now;
                this.transferCount = 0;
                this.lastLineId = currentLineId;
                System.out.println("[BİLET] Yeni Yolculuk Başladı (Tam Bilet). Ücret: " + finalAmount + " TL. Sayaç sıfırlandı.");
            }
            return true;
        }

        System.out.println("[BİLET HATA] Yetersiz Bakiye!");
        return false;
    }
}
