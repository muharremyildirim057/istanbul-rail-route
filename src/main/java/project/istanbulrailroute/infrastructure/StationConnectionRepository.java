package project.istanbulrailroute.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.istanbulrailroute.domain.models.StationConnection;

import java.util.Optional;
@Repository
public interface StationConnectionRepository extends JpaRepository<StationConnection,Long> {
    Optional<StationConnection> findFirstByStartStationIdAndTargetStationId(Long startId, Long targetId);
}
