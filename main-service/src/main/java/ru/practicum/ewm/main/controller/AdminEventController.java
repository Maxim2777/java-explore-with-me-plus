package ru.practicum.ewm.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.EventFullDto;
import ru.practicum.ewm.main.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.main.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) List<Long> users,
                                                        @RequestParam(required = false) List<String> states,
                                                        @RequestParam(required = false) List<Long> categories,
                                                        @RequestParam(required = false) String rangeStart,
                                                        @RequestParam(required = false) String rangeEnd,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> update(@PathVariable Long eventId,
                                               @RequestBody @Valid UpdateEventAdminRequest dto) {
        return ResponseEntity.ok(eventService.updateEventByAdmin(eventId, dto));
    }
}

