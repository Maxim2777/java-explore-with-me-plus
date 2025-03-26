package ru.practicum.ewm.main.service;

import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.dto.params.EventParamsAdmin;
import ru.practicum.ewm.main.dto.params.EventParamsPublic;

import java.util.List;

public interface EventService {

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto createUserEvent(Long userId, NewEventDto dto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventShortDto> getPublicEvents(EventParamsPublic params);

    EventFullDto getEventById(Long id, String ip, String uri);

    List<EventFullDto> getEventsByAdmin(EventParamsAdmin params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto);

}