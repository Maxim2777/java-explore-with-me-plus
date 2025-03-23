package ru.practicum.ewm.main.service;

import ru.practicum.ewm.main.dto.*;

import java.util.List;

public interface EventService {

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto createUserEvent(Long userId, NewEventDto dto);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        Boolean onlyAvailable,
                                        String sort,
                                        int from,
                                        int size);

    EventFullDto getEventById(Long id, String ip, String uri);

    List<EventFullDto> getEventsByAdmin(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        String rangeStart,
                                        String rangeEnd,
                                        int from,
                                        int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto);

}