package project.istanbulrailroute.domain.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.istanbulrailroute.domain.models.enums.LineType;
import project.istanbulrailroute.domain.models.enums.StationStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lines")
@Data
@NoArgsConstructor
public class Line {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;       // Benzersiz durak kodu veya adı

    @Column(nullable = false, unique = true)
    private String name;     // Durak adı (Ör: Yenikapı)

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type",nullable = false)
    private LineType type;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private List<StationConnection> connections = new ArrayList<>();


}