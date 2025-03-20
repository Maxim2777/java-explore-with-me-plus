package ru.practicum.ewm.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.mapper.EndPointHitMapper;
import ru.practicum.ewm.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;
    private final EndPointHitMapper mapper;

    @Override
    public void save(EndpointHitDto hitDto) {
        repository.save(mapper.mapToHit(hitDto));
    }

    @Override
    public List<ViewStatsDto> findStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<ViewStatsDto> statsList;

        if (uris == null && !unique) {
            statsList = repository.findStatsByTimestamp(start, end);
        } else if (uris == null) {
            statsList = repository.findStatsByTimestampAndUnique(start, end);
        } else if (!unique) {
            statsList = repository.findStatsByTimestampAndUri(start, end, uris);
        } else {
            statsList = repository.findStatsByTimestampAndUniqueAndUri(start, end, uris);
        }

        return statsList;
    }
}