package project.istanbulrailroute.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Data
@Table(name = "station_connections")
@NoArgsConstructor
public class StationConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "start_id")
    @JsonIgnore
    @ToString.Exclude
    private Station startStation;

    @ManyToOne
    @JoinColumn(name = "target_id")
    @JsonIgnore
    @ToString.Exclude
    private Station targetStation;

    @Column(name = "travel_time", nullable = false)
    private double duration; // Dakika bazlı ağırlık

    @ManyToOne
    @JoinColumn(name = "line_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Line line; // Hangi hat?

}
