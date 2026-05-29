package project.istanbulrailroute.domain.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import project.istanbulrailroute.domain.models.enums.StationStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;       // Benzersiz durak kodu veya adı

    @Column(nullable = false, unique = true)
    private String name;     // Durak adı (Ör: Yenikapı)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationStatus status = StationStatus.ACTIVE;   // "Operational", "OutOfService", "Maintenance" vb.

    @OneToMany(mappedBy = "startStation", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<StationConnection> connections = new ArrayList<>();

}