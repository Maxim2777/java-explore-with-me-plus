package ru.practicum.ewm.client;

import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class StatClient {
    private final RestClient restClient;

    public StatClient(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void sendHit(EndpointHitDto hitDto) {
        restClient.post()
                .uri("/hit")
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);

        // Если uris не пустой, добавляем в параметры запроса
        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", String.join(",", uris));
        }

        String uri = uriBuilder.toUriString();

        List<ViewStatsDto> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return response != null ? response : List.of(); // Если ответ null, возвращаем пустой список
    }
}