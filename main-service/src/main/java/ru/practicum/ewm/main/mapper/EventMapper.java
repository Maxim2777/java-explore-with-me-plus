package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.model.*;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEntity(NewEventDto dto, Long initiatorId) {
        return Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .categoryId(dto.getCategory())
                .location(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                .eventDate(LocalDateTime.parse(dto.getEventDate().replace(" ", "T")))
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .paid(dto.isPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.isRequestModeration())
                .initiatorId(initiatorId)
                .build();
    }

    public static EventFullDto toFullDto(Event event, CategoryDto category, UserShortDto initiator, long confirmed, long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(category)
                .paid(event.isPaid())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .initiator(initiator)
                .confirmedRequests(confirmed)
                .views(views)
                .build();
    }

    public static EventShortDto toShortDto(Event event, CategoryDto category, UserShortDto initiator, long confirmed, long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(category)
                .paid(event.isPaid())
                .eventDate(event.getEventDate())
                .initiator(initiator)
                .confirmedRequests(confirmed)
                .views(views)
                .build();
    }
}