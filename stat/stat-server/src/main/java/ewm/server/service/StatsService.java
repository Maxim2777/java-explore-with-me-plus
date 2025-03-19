package ewm.server.service;

import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import ewm.server.model.EndpointHit;
import ewm.server.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;

    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = new EndpointHit(null, hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());
        statsRepository.save(hit);
    }

    public List<ViewStatsDto> getStats(String start, String end) {
        // Тут нужно добавить SQL-запрос для агрегации статистики
        return statsRepository.findAll().stream()
                .map(hit -> new ViewStatsDto(hit.getApp(), hit.getUri(), 1))
                .collect(Collectors.toList());
    }
}