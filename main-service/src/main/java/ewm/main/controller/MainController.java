package ewm.main.controller;

import ewm.client.StatClient;
import ewm.dto.EndpointHitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {
    private final StatClient statClient;

    @PostMapping("/track")
    public void trackEvent(@RequestParam String uri, @RequestParam String ip) {
        EndpointHitDto hitDto = new EndpointHitDto("main-service", uri, ip, LocalDateTime.now());
        statClient.sendHit(hitDto);
    }
}
