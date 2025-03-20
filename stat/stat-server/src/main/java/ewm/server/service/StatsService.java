package ewm.server.service;

import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import ewm.server.model.EndpointHit;
import ewm.server.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;

    // Сохранение информации о посещении
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = new EndpointHit(null, hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());
        statsRepository.save(hit);
    }

    // Получение статистики (с поддержкой `uris` и `unique`)
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        // 📌 Преобразуем строки в LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);

        // 📌 Если нужен учёт только уникальных IP, вызываем `getUniqueStats()`
        if (unique) {
            return statsRepository.getUniqueStats(startDate, endDate, uris);
        } else {
            return statsRepository.getAllStats(startDate, endDate, uris);
        }
    }
}