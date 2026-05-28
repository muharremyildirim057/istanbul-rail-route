package project.istanbulrailroute.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.istanbulrailroute.domain.models.Station;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    boolean existsByName(String name);



}
