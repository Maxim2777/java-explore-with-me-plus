package ru.practicum.ewm.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

    private final RequestService requestService;

    /**
     * Получение всех заявок пользователя
     */
    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    /**
     * Добавление новой заявки на участие в событии
     */
    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        return ResponseEntity.status(201).body(requestService.addRequest(userId, eventId));
    }

    /**
     * Отмена заявки пользователем
     */
    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.cancelRequest(userId, requestId));
    }
}
