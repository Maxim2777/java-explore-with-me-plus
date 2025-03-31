package ru.practicum.ewm.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
        SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app.name, e.uri.uri, COUNT(e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND e.app.name = :app
        GROUP BY e.app.name, e.uri.uri
        ORDER BY COUNT(e.ip) DESC
    """)
    List<ViewStatsDto> findStatsByTimestamp(@Param("app") String app, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app.name, e.uri.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
          AND e.app.name = :app
        GROUP BY e.app.name, e.uri.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStatsDto> findStatsByTimestampAndUnique(@Param("app") String app, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app.name, e.uri.uri, COUNT(e.ip))
        FROM EndpointHit e
        WHERE e.uri.uri IN :uris
          AND e.timestamp BETWEEN :start AND :end
          AND e.app.name = :app
        GROUP BY e.app.name, e.uri.uri
        ORDER BY COUNT(e.ip) DESC
    """)
    List<ViewStatsDto> findStatsByTimestampAndUri(@Param("app") String app, LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("""
        SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app.name, e.uri.uri, COUNT(DISTINCT e.ip))
        FROM EndpointHit e
        WHERE e.uri.uri IN :uris
          AND e.timestamp BETWEEN :start AND :end
          AND e.app.name = :app
        GROUP BY e.app.name, e.uri.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStatsDto> findStatsByTimestampAndUniqueAndUri(@Param("app") String app, LocalDateTime start, LocalDateTime end, List<String> uris);
}