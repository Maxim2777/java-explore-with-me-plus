package ru.practicum.ewm.main.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.dto.params.EventParamsAdmin;
import ru.practicum.ewm.main.dto.params.EventParamsPublic;

import java.util.List;

public interface EventService {

    List<EventShortDto> getUserEvents(Long userId, AdminUserParam params);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto createUserEvent(Long userId, NewEventDto dto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventShortDto> getPublicEvents(EventParamsPublic params, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventFullDto> getEventsByAdmin(EventParamsAdmin params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto);

}