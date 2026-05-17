package project.istanbulrailroute.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@NoArgsConstructor
public class StationConnection {

    @Id
    @GeneratedValue
    private Long id; // Spring Data Neo4j'nin güvenli güncellemeler için aradığı eksik ID alanı!

    private String lineName;   // M2, Marmaray, T1 vb. (Cheapest Fare stratejisi için)
    private int travelTime;    // Dakika cinsinden süre (Shortest Time stratejisi için)
    private double fare;       // Hat bazlı tarife/ücret bilgisi (Cheapest Fare için)

    @TargetNode
    private Station targetStation; // Bağlanılan hedef durak
}
