package project.istanbulrailroute.infrastructure;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.istanbulrailroute.domain.models.Line;

import java.util.Optional;

@Repository
public interface LineRepository extends JpaRepository<Line, Long> {
    // İleride hat ismine göre arama yapmak istersek diye hazır durabilir
    Optional<Line> findByName(String name);
}
