package ru.practicum.ewm.main.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.dto.params.EventParamsAdmin;
import ru.practicum.ewm.main.dto.params.EventParamsPublic;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.exception.ValidationException;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.model.enums.EventState;
import ru.practicum.ewm.main.model.enums.ParticipationRequestStatus;
import ru.practicum.ewm.main.model.enums.RequestStatus;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.ParticipationRequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParticipationRequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final StatClient statClient;

    // --- PRIVATE API ---

    @Override
    public List<EventShortDto> getUserEvents(Long userId, AdminUserParam params) {
        int from = params.getFrom();
        int size = params.getSize();

        PageRequest page = PageRequest.of(from / size, size);
        checkUser(userId);

        BooleanExpression byUserId = QEvent.event.initiator.id.eq(userId);
        List<Event> events = eventRepository.findAll(byUserId, page).getContent();

        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Long> confrmedMap = getConfirmedRequests(events);

        return events
                .stream()
                .map(event -> EventMapper
                        .toShortDto(event, confrmedMap.get(event.getId()), viewsMap.get(event.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        checkUser(userId);

        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not the owner of this event");
        }

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confrmedMap = getConfirmedRequests(List.of(event));

        return EventMapper.entityToFullDto(event, confrmedMap.get(event.getId()), viewsMap.get(event.getId()));
    }

    @Override
    @Transactional
    public EventFullDto createUserEvent(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден!"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id: " + dto.getCategory() + " не найдена!"));

        if (dto.getEventDate().isBefore(LocalDateTime.now()) ||
            !dto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата и время события не могут в прошлом или" +
                                          " раньше, чем через два часа : " + dto.getEventDate());
        }

        Event event = EventMapper.toEntity(dto, userId);
        Event savedEvent = eventRepository.save(event);

        return EventMapper.toFullDto(savedEvent,
                new CategoryDto(category.getId(), category.getName()),
                new UserShortDto(user.getId(), user.getName()),
                0, 0);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = getEventById(eventId);

        checkUser(userId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not the owner of this event");
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя изменить опубликованное событие!");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());

        if (dto.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(dto.getEventDate().replace(" ", "T"));
            if (newEventDate.isBefore(LocalDateTime.now()) ||
                !newEventDate.isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Дата и время события не могут в прошлом или" +
                                              " раньше, чем через два часа : " + dto.getEventDate());
            }
            event.setEventDate(newEventDate);
        }

        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id: " + dto.getCategory() + " не найдена!"));
            event.setCategory(category);
        }

        if ("SEND_TO_REVIEW".equals(dto.getStateAction())) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(event));

        eventRepository.save(event);

        return EventMapper.entityToFullDto(event, confirmedMap.get(event.getId()), viewsMap.get(event.getId()));
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsByUserIdAndEventId(Long userId, Long eventId) {
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        eventRepository.findOne(byEventId)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено!"));

        checkUser(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        System.out.println(requests + "requests");
        for (ParticipationRequest request : requestRepository.findAll()) {
            System.out.println(request.toString() + "request");
        }

        return requests.stream().map(ParticipationRequestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest requestUpdate) {
        Event event = getEventById(eventId);

        checkUser(userId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not the owner of this event");
        }

        List<Long> requestIds = requestUpdate.getRequestIds();
        RequestStatus status = requestUpdate.getStatus();
        int requestCount = requestIds.size();


        int limit = event.getParticipantLimit();

     /*  if (limit == 0 || !event.isRequestModeration()) {
            throw new ConflictException("Подтверждение заявок не требуется");
        }
       */

        List<ParticipationRequest> requests = requestRepository.findAllByEventIdIn(requestIds);

        long currentConfirmed = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);

        if (currentConfirmed == limit) {
            throw new ConflictException("Достигнут лимит по заявкам на данное событие: " + event);
        }

        for (ParticipationRequest request : requests) {
            if (!request.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания." +
                                            " Заявка имеет статус: " + request.getStatus());
            }
        }

        List<ParticipationRequest> updatedRequests = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        switch (status) {
            case CONFIRMED: {
                if (limit == 0 || !event.isRequestModeration() || currentConfirmed + requestCount <= limit) {
                    for (ParticipationRequest request : requests) {
                        request.setStatus(ParticipationRequestStatus.CONFIRMED);
                        updatedRequests.add(request);
                        confirmedRequests.add(ParticipationRequestMapper.toDto(request));
                    }
                } else if (currentConfirmed >= limit) {
                    throw new ConflictException("Достигнут лимит по заявкам на данное событие: " + event);
                } else {
                    for (ParticipationRequest request : requests) {
                        if (limit > currentConfirmed) {
                            request.setStatus(ParticipationRequestStatus.CONFIRMED);
                            updatedRequests.add(request);
                            confirmedRequests.add(ParticipationRequestMapper.toDto(request));
                            currentConfirmed++;
                        } else {
                            request.setStatus(ParticipationRequestStatus.REJECTED);
                            updatedRequests.add(request);
                            rejectedRequests.add(ParticipationRequestMapper.toDto(request));
                        }
                    }
                }
                break;
            }
            case REJECTED: {
                for (ParticipationRequest request : requests) {
                    request.setStatus(ParticipationRequestStatus.REJECTED);
                    updatedRequests.add(request);
                    rejectedRequests.add(ParticipationRequestMapper.toDto(request));
                }
            }
            break;
        }

        requestRepository.saveAll(updatedRequests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;
    }

    // --- PUBLIC API ---

    @Override
    public List<EventShortDto> getPublicEvents(EventParamsPublic params, HttpServletRequest request) {
        PageRequest page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        BooleanBuilder where = new BooleanBuilder();
        QEvent event = QEvent.event;

        String text = params.getText();
        List<Long> categories = params.getCategories();
        boolean onlyAvailable = params.isOnlyAvailable();
        String sort = params.getSort();
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (params.getRangeStart() != null) {
            rangeStart = LocalDateTime.parse(params.getRangeStart().replace(" ", "T"));
        }

        if (params.getRangeEnd() != null) {
            rangeEnd = LocalDateTime.parse(params.getRangeEnd().replace(" ", "T"));
        }

        where.and(event.state.in(EventState.PUBLISHED));

        if (text != null && !text.isEmpty()) {
            where.or(event.annotation.lower().like("%" + text.toLowerCase() + "%")
                    .or(event.description.lower().like("%" + text.toLowerCase() + "%")));
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst().equals(0L)) {
                throw new ValidationException("Неверный список идентификаторов категорий - " + categories);
            }
            where.and(event.category.id.in(categories));
        }

        if (params.getPaid() != null) {
            where.and(event.paid.eq(params.getPaid()));
        }

        if (rangeStart != null) {
            where.and(event.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            where.and(event.eventDate.before(rangeEnd));
        }

        if (rangeStart == null && rangeEnd == null) {
            where.and(event.eventDate.after(LocalDateTime.now()));
        }

        List<Event> events = eventRepository.findAll(where, page).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        if (onlyAvailable) {
            events = events
                    .stream()
                    .filter(e -> e.getParticipantLimit() > confirmedRequests
                            .getOrDefault(e.getId(), 0L))
                    .toList();
        }
        List<EventShortDto> eventShorts;

        Map<Long, Long> confirmedMap = getConfirmedRequests(events);
        Map<Long, Long> viewsMap = getViews(events);

        List<String> uris = events
                .stream()
                .map(e1 -> request.getRequestURI() + "/" + e1.getId())
                .toList();

        eventShorts = events
                .stream()
                .map(e -> EventMapper.toShortDto(e, confirmedMap.get(e.getId()), viewsMap.get(e.getId())))
                .collect(Collectors.toList());


        statClient.sendHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(String.valueOf(uris))
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        if (sort == null) {
            return eventShorts;
        }

        return switch (sort) {
            case "VIEWS" -> eventShorts
                    .stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews))
                    .collect(Collectors.toList());
            case "EVENT_DATE" -> eventShorts
                    .stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .collect(Collectors.toList());
            default -> eventShorts;
        };
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = getEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event is not published");
        }

        Map<Long, Long> viewsMap = getViews(List.of(event));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(event));

        statClient.sendHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        return EventMapper.entityToFullDto(event,
                confirmedMap.get(event.getId()), viewsMap.get(event.getId()));
    }

    // --- ADMIN API ---

    @Override
    public List<EventFullDto> getEventsByAdmin(EventParamsAdmin params) {
        PageRequest page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        BooleanBuilder where = new BooleanBuilder();
        QEvent event = QEvent.event;

        List<Long> users = params.getUsers();
        List<Long> categories = params.getCategories();
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (params.getStates() != null && !params.getStates().isEmpty()) {
            List<EventState> states = params.getStates().stream().map(EventState::valueOf).collect(Collectors.toList());
            where.and(event.state.in(states));
        }

        if (users != null && !users.isEmpty()) {
            if (users.size() == 1 && users.getFirst() == 0L) {
                throw new ValidationException("Неверный список идентификаторов категорий - " + categories);
            }
            where.and(event.initiator.id.in(users));
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst() == 0L) {
                throw new ValidationException("Неверный список идентификаторов категорий - " + categories);
            }
            where.and(event.category.id.in(categories));
        }

        if (params.getRangeStart() != null) {
            rangeStart = LocalDateTime.parse(params.getRangeStart().replace(" ", "T"));
        }

        if (params.getRangeEnd() != null) {
            rangeEnd = LocalDateTime.parse(params.getRangeEnd().replace(" ", "T"));
        }

        if (rangeStart != null) {
            where.and(event.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            where.and(event.eventDate.before(rangeEnd));
        }

        List<Event> events = eventRepository.findAll(where, page).getContent();

        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Long> confirmedMap = getConfirmedRequests(events);

        return events
                .stream()
                .map(e -> EventMapper.entityToFullDto(e, confirmedMap.get(e.getId()), viewsMap.get(e.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = getEventById(eventId);

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id: " + dto.getCategory() + " не найдена!"));
            event.setCategory(category);
        }
        if (dto.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(dto.getEventDate().replace(" ", "T"));
            if (newEventDate.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события не может в прошлом: " + dto.getEventDate());
            }
            event.setEventDate(newEventDate);
        }

        if (dto.getLocation() != null) {
            event.setLocation(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()));
        }

        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());

        if ("PUBLISH_EVENT".equals(dto.getStateAction())) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new ConflictException("Event must be in PENDING state to publish");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equals(dto.getStateAction())) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Cannot reject a published event");
            }
            event.setState(EventState.CANCELED);
        }

        Event savedEvent = eventRepository.save(event);

        Map<Long, Long> viewsMap = getViews(List.of(savedEvent));
        Map<Long, Long> confirmedMap = getConfirmedRequests(List.of(savedEvent));

        return EventMapper
                .entityToFullDto(event, confirmedMap.get(savedEvent.getId()), viewsMap.get(savedEvent.getId()));
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime startDate = events
                .stream()
                .map(Event::getCreatedOn)
                .toList()
                .stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);

        String start = Objects.requireNonNull(startDate).format(FORMATTER);
        String end = LocalDateTime.now().format(FORMATTER);

        List<ViewStatsDto> views = statClient.getStats(start, end, uris, true);

        Map<Long, Long> map = events
                .stream()
                .collect(Collectors.toMap(Event::getId, e -> 0L, (a, b) -> b));

        if (!views.isEmpty()) {
            views.forEach(v -> map.put(Long.parseLong(v.getUri().split("/", 0)[2]),
                    v.getHits()));
        }

        return map;
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        List<ParticipationRequest> requests = requestRepository
                .findAllByEventIdInAndStatus(eventIds, ParticipationRequestStatus.CONFIRMED);

        Map<Long, List<ParticipationRequest>> result = requests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId()));

        return eventIds
                .stream()
                .collect(Collectors
                        .toMap(eventId -> eventId,
                                eventId -> (long) result.getOrDefault(eventId,
                                        List.of()).size(), (a, b) -> b));
    }

    private Event getEventById(Long eventId) {
        BooleanExpression byEventId = QEvent.event.id.eq(eventId);
        return eventRepository.findOne(byEventId)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено!"));
    }

    private void checkUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден!"));
    }
}