package ru.practicum.ewm.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.EventShortDto;
import ru.practicum.ewm.main.dto.params.EventParamsPublic;
import ru.practicum.ewm.main.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;


    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@Valid EventParamsPublic params,
                                                         HttpServletRequest request) {
        log.info("Get public events. params: {}", params);
        return ResponseEntity.ok(eventService.getPublicEvents(params, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long id,
                                                     HttpServletRequest request) {
        log.info("Get public event. id: {}", id);
        return ResponseEntity.ok(eventService.getEventById(id, request));
    }
}