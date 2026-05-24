package project.istanbulrailroute.infrastructure;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.istanbulrailroute.domain.models.Passenger;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Passenger,Long> {
    boolean existsByUsername(String username);

    Optional<Passenger> findByUsername(String username);
}
