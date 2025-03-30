package ru.practicum.ewm.server.mapper;

import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.model.App;
import ru.practicum.ewm.server.model.EndpointHit;
import ru.practicum.ewm.server.model.Uri;

public class StatsMapper {

    public static EndpointHit toEntity(EndpointHitDto dto, App app, Uri uri) {
        return EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .app(hit.getApp().getName())
                .uri(hit.getUri().getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

    public static ViewStatsDto toViewStats(EndpointHit hit, long hits) {
        return new ViewStatsDto(hit.getApp().getName(), hit.getUri().getUri(), hits);
    }
}
