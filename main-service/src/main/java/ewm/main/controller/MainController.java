package ewm.main.controller;

import ewm.client.StatClient;
import ewm.dto.EndpointHitDto;
import ewm.dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {
    private final StatClient statClient;

    // POST /track - отправка статистики
    @PostMapping("/track")
    public void trackEvent(@RequestParam String uri, @RequestParam String ip) {
        EndpointHitDto hitDto = new EndpointHitDto("main-service", uri, ip, LocalDateTime.now());
        statClient.sendHit(hitDto);
    }

    // GET /stats - получение статистики
    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);

        return statClient.getStats(start, end, uris, unique);
    }
}