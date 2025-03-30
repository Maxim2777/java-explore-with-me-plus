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
            WHERE e.timestamp
            BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByTimestamp(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM EndpointHit AS e
            WHERE e.timestamp
            BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByTimestampAndUnique(LocalDateTime start, LocalDateTime end);

    // Исправленный метод: app убрали, жестко задали "main-service", группировка только по URI
    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto('main-service', e.uri, COUNT(e.ip))
            FROM EndpointHit AS e
            WHERE e.uri IN :uris
            AND e.timestamp
            BETWEEN :start AND :end
            GROUP BY e.uri
            ORDER BY COUNT(e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByTimestampAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    // Исправленный метод для unique=true: тоже убрали app
    @Query("""
            SELECT new ru.practicum.ewm.dto.ViewStatsDto('main-service', e.uri, COUNT(DISTINCT e.ip))
            FROM EndpointHit AS e
            WHERE e.uri IN :uris
            AND e.timestamp
            BETWEEN :start AND :end
            GROUP BY e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStatsDto> findStatsByTimestampAndUniqueAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);
}