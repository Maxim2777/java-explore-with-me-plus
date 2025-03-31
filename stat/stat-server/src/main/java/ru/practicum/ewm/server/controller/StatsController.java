package ru.practicum.ewm.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody EndpointHitDto hitDto) {
        log.info("StatsController - сохранение информации о запросе к эндпоинту: {}", hitDto);
        service.save(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") boolean unique) {

        // Декодируем параметры start и end
        String decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
        String decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);

        // Преобразуем строки в LocalDateTime
        LocalDateTime startDate = LocalDateTime.parse(decodedStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime endDate = LocalDateTime.parse(decodedEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("StatsController - получение статистики по посещениям с {} по {} к эндпоинтам: {}, уникальность - {}",
                startDate, endDate, uris, unique);

        // ✅ Добавлена валидация дат
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Параметр start не может быть позже end");
        }

        return service.findStats(startDate, endDate, uris, unique);
    }
}