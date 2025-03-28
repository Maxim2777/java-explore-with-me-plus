package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.ParticipationRequest;
import ru.practicum.ewm.main.model.enums.ParticipationRequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    // Получить все заявки пользователя
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    // Проверить наличие заявки от пользователя на событие
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    // Получить все заявки на событие
    List<ParticipationRequest> findAllByEventId(Long eventId);

    long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    List<ParticipationRequest> findAllByEventIdIn(List<Long> eventIds);

    List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds, ParticipationRequestStatus status);
}