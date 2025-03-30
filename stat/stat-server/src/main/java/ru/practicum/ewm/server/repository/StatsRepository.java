package ru.practicum.ewm.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
        SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app.name, e.uri.uri, COUNT(e.id))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND (:uris IS NULL OR e.uri.uri IN :uris)
        GROUP BY e.app.name, e.uri.uri
        ORDER BY COUNT(e.id) DESC
    """)
    List<ViewStatsDto> getStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query("""
        SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app.name, e.uri.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND (:uris IS NULL OR e.uri.uri IN :uris)
        GROUP BY e.app.name, e.uri.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStatsDto> getUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}