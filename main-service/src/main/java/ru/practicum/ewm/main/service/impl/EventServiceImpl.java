package ru.practicum.ewm.main.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Query;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.StatelessSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.querydsl.QSort;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.UserRepository;
import ru.practicum.ewm.main.service.CategoryService;
import ru.practicum.ewm.main.service.EventService;
import ru.practicum.ewm.main.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final StatClient statClient;
    private final CategoryService categoryService;
    private final UserService userService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // --- PRIVATE API ---

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {

        PageRequest page = PageRequest.of(from / size, size);
        BooleanExpression byUserId = QEvent.event.initiator.id.eq(userId);

        Iterable<Event> events = eventRepository.findAll(byUserId, page);

        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (Event e : events) {
            EventShortDto shortDto = EventMapper.toShortDto(e,
                    0, getViews(e.getId(), LocalDateTime.now().minusYears(1), LocalDateTime.now()));
            eventShortDtos.add(shortDto);
        }

        return eventShortDtos;
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("User is not the owner of this event");
        }

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategory().getId()),
                userService.getShortById(event.getInitiator().getId()),
                0, 0);
    }

    @Override
    @Transactional
    public EventFullDto createUserEvent(Long userId, NewEventDto dto) {

        System.out.println("createUserEvent" + dto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден!"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id: " + dto.getCategory() + " не найдена!"));

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
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("User is not the owner of this event");
        }

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) event.setEventDate(LocalDateTime.parse(dto.getEventDate().replace(" ", "T")));
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

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategory().getId()),
                userService.getShortById(event.getInitiator().getId()),
                0, 0);
    }

    // --- PUBLIC API ---

    @Override
    public List<EventShortDto> getPublicEvents(EventParamsPublic params) {


        BooleanBuilder where = new BooleanBuilder();
        QEvent event = QEvent.event;

        String text = params.getText();
        List<Long> categories = params.getCategories();

        boolean paid = params.getPaid();
        LocalDateTime rangeStart = null;

        LocalDateTime rangeEnd = null;

        if (params.getRangeStart() != null) {
            rangeStart = LocalDateTime.parse(params.getRangeStart().replace(" ", "T"));

        }

        if (params.getRangeEnd() != null) {
            rangeEnd = LocalDateTime.parse(params.getRangeEnd().replace(" ", "T"));
        }

        boolean onlyAvailable = params.isOnlyAvailable();
        String sort = params.getSort();

        PageRequest page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), Sort.unsorted());


        where.and(event.state.in(EventState.PUBLISHED));

        if (text != null && !text.isEmpty()) {
            where.and(event.annotation.contains("%" + text.toLowerCase() + "%")
                    .or(event.description.contains("%" + text.toLowerCase() + "%")));
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.size() == 1 && categories.getFirst().equals(0L)) {
                throw new ValidationException("Неверный список идентификаторов категорий - " + categories);
            }
            where.and(event.category.id.in(categories));
        }

        where.and(event.paid.eq(paid));

        if (rangeStart != null ) {
            where.and(event.eventDate.after(rangeStart));

        }

        if (rangeEnd != null ) {

            where.and(event.eventDate.before(rangeEnd));
        }


        Iterable<Event> events = eventRepository.findAll(where, page);

        List<EventShortDto> eventShortDtos = new ArrayList<>();

        for (Event e : events) {
            eventShortDtos.add(EventMapper
                    .toShortDto(e, 0, getViews(e.getId(),
                            LocalDateTime.now().minusYears(1), LocalDateTime.now())));
        }
        return eventShortDtos;
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long id, String ip, String uri) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event is not published");
        }

        statClient.sendHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());

        long views = getViews(id, LocalDateTime.now().minusYears(1), LocalDateTime.now());

        return EventMapper.toFullDto(event,
                categoryService.getById(event.getCategory().getId()),
                userService.getShortById(event.getInitiator().getId()),
                0, views);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(EventParamsAdmin params) {
        PageRequest page = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), Sort.unsorted());

        BooleanBuilder where = new BooleanBuilder();

        QEvent event = QEvent.event;


        List<Long> users = params.getUsers();
        List<EventState> states = params.getStates().stream().map(EventState::valueOf).toList();
        List<Long> categories = params.getCategories();
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;




        if (!states.isEmpty()) {
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




        if (rangeStart != null ) {
            where.and(event.eventDate.after(rangeStart));

        }

        if (rangeEnd != null ) {

            where.and(event.eventDate.before(rangeEnd));
        }

        Iterable<Event> events = eventRepository.findAll(where, page);


        List<EventFullDto> eventFullDtos = new ArrayList<>();

        for (Event e : events) {
            eventFullDtos.add(EventMapper
                    .EntityToFullDto(e, 0, getViews(e.getId(),
                            LocalDateTime.now().minusYears(1), LocalDateTime.now())));
        }
        return eventFullDtos;
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


/*    @Override
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
    }*/

    // НЕ УВЕРЕН КАК ТУТ КОРРЕКТНО ДЕЛАТЬ СТАРАЯ ВЕРСИЯ ЗАКОМИЧЕНА ВЫШЕ
  /*  @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, int from, int size) {

        // Обработка временного диапазона
        LocalDateTime start = rangeStart != null
                ? LocalDateTime.parse(rangeStart.replace(" ", "T"))
                : LocalDateTime.MIN;
        LocalDateTime end = rangeEnd != null
                ? LocalDateTime.parse(rangeEnd.replace(" ", "T"))
                : LocalDateTime.MAX;

        // Сохраняем effectively final переменные
        List<Long> finalUsers = (users != null && users.contains(0L)) ? null : users;
        List<Long> finalCategories = (categories != null && categories.contains(0L)) ? null : categories;

        return eventRepository.findAll(PageRequest.of(from / size, size)).stream()
                .filter(event ->
                        (finalUsers == null || finalUsers.contains(event.getInitiatorId())) &&
                                (states == null || states.contains(event.getState().name())) &&
                                (finalCategories == null || finalCategories.contains(event.getCategoryId())) &&
                                (event.getEventDate().isAfter(start) && event.getEventDate().isBefore(end))
                )
                .map(event -> EventMapper.toFullDto(
                        event,
                        categoryService.getById(event.getCategoryId()),
                        userService.getShortById(event.getInitiatorId()),
                        0,
                        getViews(event.getId(), start, end)
                ))
                .collect(Collectors.toList());
    }*/


    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id: " + eventId + " не найдено!"));

        if (dto.getTitle() != null) event.setTitle(dto.getTitle());
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id: " + dto.getCategory() + " не найдена!"));
            event.setCategory(category);
        }
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
                categoryService.getById(event.getCategory().getId()),
                userService.getShortById(event.getInitiator().getId()),
                0, getViews(event.getId(), LocalDateTime.now().minusYears(1), LocalDateTime.now()));
    }
}