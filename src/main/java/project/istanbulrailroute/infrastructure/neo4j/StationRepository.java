package project.istanbulrailroute.infrastructure.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import project.istanbulrailroute.domain.models.Station;

@Repository
public interface StationRepository extends Neo4jRepository<Station, String> {
    // İleride Strategy Pattern sorgularımızı (Cypher) buraya ekleyeceğiz.
}
