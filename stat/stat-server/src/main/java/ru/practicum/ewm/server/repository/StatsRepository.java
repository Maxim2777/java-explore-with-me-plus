package ru.practicum.ewm.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(e.ip))
            FROM EndpointHit AS e
            WHERE e.app = :app
              AND e.timestamp BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByAppAndTimestamp(String app, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM EndpointHit AS e
            WHERE e.app = :app
              AND e.timestamp BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByAppAndTimestampAndUnique(String app, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(e.ip))
            FROM EndpointHit AS e
            WHERE e.app = :app
              AND e.uri IN :uris
              AND e.timestamp BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByAppAndTimestampAndUri(String app, LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM EndpointHit AS e
            WHERE e.app = :app
              AND e.uri IN :uris
              AND e.timestamp BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByAppAndTimestampAndUniqueAndUri(String app, LocalDateTime start, LocalDateTime end, List<String> uris);
}