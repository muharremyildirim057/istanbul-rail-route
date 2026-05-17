package project.istanbulrailroute.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Station")
@Data
@NoArgsConstructor
public class Station {

    @Id
    private String id;       // Benzersiz durak kodu veya adı
    private String name;     // Durak adı (Ör: Yenikapı)
    private String status;   // "Operational", "OutOfService", "Maintenance" vb.

    // Bir durağın bağlı olduğu diğer duraklar (Ağırlıklı Ayrıtlar listesi)
    @Relationship(type = "CONNECTED_TO", direction = Relationship.Direction.OUTGOING)
    private List<StationConnection> connections = new ArrayList<>();

    // Yardımcı metot: Yeni bir durağa bağlantı (kenar) eklemek için
    public void addConnection(Station target, String lineName, int travelTime, double fare) {
        StationConnection connection = new StationConnection();
        connection.setLineName(lineName);
        connection.setTravelTime(travelTime);
        connection.setFare(fare);
        connection.setTargetStation(target);
        this.connections.add(connection);
    }
}