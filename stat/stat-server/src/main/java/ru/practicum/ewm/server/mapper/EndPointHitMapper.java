package ru.practicum.ewm.server.mapper;

import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.server.model.App;
import ru.practicum.ewm.server.model.EndpointHit;
import ru.practicum.ewm.server.model.Uri;

public class EndPointHitMapper {

    public static EndpointHit mapToHit(EndpointHitDto dto, App app, Uri uri) {
        return EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}