package project.istanbulrailroute.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.istanbulrailroute.domain.models.Station;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    @Query(value = "WITH RECURSIVE route AS (" +
            "  SELECT target_id, line_id, ARRAY[start_id, target_id] as path, travel_time as total_duration " +
            "  FROM station_connections WHERE start_id = :startId " +
            "  UNION ALL " +
            "  SELECT sc.target_id, sc.line_id, r.path || sc.target_id, " +
            "         r.total_duration + sc.travel_time + (CASE WHEN r.line_id <> sc.line_id THEN 5.0 ELSE 0.0 END) " +
            "  FROM route r " +
            "  JOIN station_connections sc ON r.target_id = sc.start_id " +
            // HAYAT KURTARAN DOKUNUŞ BURADA: AND array_length(r.path, 1) < 20
            "  WHERE NOT sc.target_id = ANY(r.path) AND array_length(r.path, 1) < 20" +
            ") " +
            "SELECT unnest(path) FROM (" +
            "  SELECT path FROM route WHERE target_id = :endId ORDER BY total_duration ASC LIMIT 1" +
            ") as shortest_route", nativeQuery = true)
    List<Long> findShortestPathIds(@Param("startId") Long startId, @Param("endId") Long endId);


    // EN AZ AKTARMA STRATEJİSİ (LEAST TRANSFER)
    @Query(value = "WITH RECURSIVE route AS (" +
            "  SELECT target_id, line_id, ARRAY[start_id, target_id] as path, " +
            "         travel_time as total_duration, 0 as transfer_count " +
            "  FROM station_connections WHERE start_id = :startId " +
            "  UNION ALL " +
            "  SELECT sc.target_id, sc.line_id, r.path || sc.target_id, " +
            "         r.total_duration + sc.travel_time, " +
            "         r.transfer_count + (CASE WHEN r.line_id <> sc.line_id THEN 1 ELSE 0 END) " +
            "  FROM route r " +
            "  JOIN station_connections sc ON r.target_id = sc.start_id " +
            "  WHERE NOT sc.target_id = ANY(r.path) AND array_length(r.path, 1) < 20" +
            ") " +
            "SELECT unnest(path) FROM (" +
            // DİKKAT: Burada ORDER BY transfer_count ASC yapıyoruz! (Önce en az aktarma, eşitse en kısa süre)
            "  SELECT path FROM route WHERE target_id = :endId ORDER BY transfer_count ASC, total_duration ASC LIMIT 1" +
            ") as least_transfer_route", nativeQuery = true)
    List<Long> findLeastTransferPathIds(@Param("startId") Long startId, @Param("endId") Long endId);

    // 3. EN AZ DURAK STRATEJİSİ (LEAST STOPS)
    @Query(value = "WITH RECURSIVE route AS (" +
            "  SELECT target_id, line_id, ARRAY[start_id, target_id] as path, travel_time as total_duration " +
            "  FROM station_connections WHERE start_id = :startId " +
            "  UNION ALL " +
            "  SELECT sc.target_id, sc.line_id, r.path || sc.target_id, " +
            "         r.total_duration + sc.travel_time + (CASE WHEN r.line_id <> sc.line_id THEN 5.0 ELSE 0.0 END) " +
            "  FROM route r " +
            "  JOIN station_connections sc ON r.target_id = sc.start_id " +
            "  WHERE NOT sc.target_id = ANY(r.path) AND array_length(r.path, 1) < 20" +
            ") " +
            "SELECT unnest(path) FROM (" +
            // DİKKAT: Burada ORDER BY array_length ile durak sayısını sayıp en az olanı seçiyoruz!
            "  SELECT path FROM route WHERE target_id = :endId ORDER BY array_length(path, 1) ASC, total_duration ASC LIMIT 1" +
            ") as least_stops_route", nativeQuery = true)
    List<Long> findLeastStopsPathIds(@Param("startId") Long startId, @Param("endId") Long endId);

    boolean existsByName(String name);


}
