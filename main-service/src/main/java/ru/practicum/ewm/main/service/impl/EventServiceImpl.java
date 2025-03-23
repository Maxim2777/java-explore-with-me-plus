package ru.practicum.ewm.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.model.Location;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.CategoryService;
import ru.practicum.ewm.main.service.EventService;
import ru.practicum.ewm.main.service.UserService;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final StatClient statClient;
    private final CategoryService categoryService;
    private final UserService userService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- PRIVATE API ---

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        return eventRepository.findAllByInitiatorId(userId).stream()
                .map(event -> EventMapper.toShortDto(event,
                        categoryService.getById(event.getCategoryId()),
                        userService.getShortById(event.getInitiatorId()),
                        0, 0))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new IllegalStateException("User is not the owner of this event");
        }

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategoryId()),
                userService.getShortById(event.getInitiatorId()),
                0, 0);
    }

    @Override
    @Transactional
    public EventFullDto createUserEvent(Long userId, NewEventDto dto) {
        Event event = EventMapper.toEntity(dto, userId);
        eventRepository.save(event);

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategoryId()),
                userService.getShortById(userId),
                0, 0);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new IllegalStateException("User is not the owner of this event");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(LocalDateTime.parse(dto.getEventDate().replace(" ", "T")));
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getCategory() != null) event.setCategoryId(dto.getCategory());

        if ("SEND_TO_REVIEW".equals(dto.getStateAction())) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategoryId()),
                userService.getShortById(event.getInitiatorId()),
                0, 0);
    }

    // --- PUBLIC API ---

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                               String sort, int from, int size) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart.replace(" ", "T")) : LocalDateTime.now().minusYears(1);
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd.replace(" ", "T")) : LocalDateTime.now();

        return eventRepository.findAll(PageRequest.of(from / size, size)).stream()
                .filter(event -> event.getState() == EventState.PUBLISHED)
                .map(event -> {
                    long views = getViews(event.getId(), start, end);
                    return EventMapper.toShortDto(event,
                            categoryService.getById(event.getCategoryId()),
                            userService.getShortById(event.getInitiatorId()),
                            0, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long id, String ip, String uri) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new IllegalStateException("Event is not published");
        }

        statClient.sendHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());

        long views = getViews(id, LocalDateTime.now().minusYears(1), LocalDateTime.now());

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategoryId()),
                userService.getShortById(event.getInitiatorId()),
                0, views);
    }

    private long getViews(Long eventId, LocalDateTime start, LocalDateTime end) {
        String formattedStart = start.format(FORMATTER);
        String formattedEnd = end.format(FORMATTER);

        List<ViewStatsDto> stats = statClient.getStats(
                formattedStart,
                formattedEnd,
                List.of("/events/" + eventId),
                false
        );

        return stats.isEmpty() ? 0 : stats.get(0).getHits();
    }

    // --- ADMIN API ---

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, int from, int size) {
        return eventRepository.findAll(PageRequest.of(from / size, size)).stream()
                .filter(event ->
                        (users == null || users.contains(event.getInitiatorId())) &&
                                (states == null || states.contains(event.getState().name())) &&
                                (categories == null || categories.contains(event.getCategoryId()))
                )
                .map(event -> EventMapper.toFullDto(event,
                        categoryService.getById(event.getCategoryId()),
                        userService.getShortById(event.getInitiatorId()),
                        0, getViews(event.getId(), LocalDateTime.now().minusYears(1), LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCategory() != null) event.setCategoryId(dto.getCategory());
        if (dto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(dto.getEventDate().replace(" ", "T")));
        }
        if (dto.getLocation() != null) {
            event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
        }
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if ("PUBLISH_EVENT".equals(dto.getStateAction())) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new IllegalStateException("Event must be in PENDING state to publish");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equals(dto.getStateAction())) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new IllegalStateException("Cannot reject a published event");
            }
            event.setState(EventState.CANCELED);
        }

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategoryId()),
                userService.getShortById(event.getInitiatorId()),
                0, getViews(event.getId(), LocalDateTime.now().minusYears(1), LocalDateTime.now()));
    }
}