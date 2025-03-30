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
                .queryParam("unique", unique);

        if (start != null && !start.isBlank()) {
            uriBuilder.queryParam("start", start);
        }
        if (end != null && !end.isBlank()) {
            uriBuilder.queryParam("end", end);
        }
        if (uris != null && !uris.isEmpty()) {
            for (String uriItem : uris) {
                uriBuilder.queryParam("uris", uriItem);
            }
        }

        String uri = uriBuilder.build().toUriString(); // Ключевая строка

        List<ViewStatsDto> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return response != null ? response : List.of();
    }
}