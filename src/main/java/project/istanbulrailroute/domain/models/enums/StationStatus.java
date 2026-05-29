package project.istanbulrailroute.domain.models.enums;


public enum StationStatus {
    OPERATIONAL,          // Aktif ve kullanımda olan duraklar
    UNDER_CONSTRUCTION,   // Henüz açılmamış, haritada yok sayılacak
    MAINTENANCE,          // Bakımda, sadece transit geçiş
    DISRUPTED,            // Arızalı/Olay var, sadece transit geçiş
    DECOMMISSIONED        // Kalıcı olarak kapatılmış, haritada yok sayılacak
}
