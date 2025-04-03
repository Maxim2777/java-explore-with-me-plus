package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventId(Long eventId, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:authorId IS NULL OR c.authorId = :authorId) " +
            "AND (:eventId IS NULL OR c.eventId = :eventId) " +
            "AND (:rangeStart IS NULL OR c.createdOn >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR c.createdOn <= :rangeEnd)")
    List<Comment> findByFilters(
            @Param("authorId") Long authorId,
            @Param("eventId") Long eventId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );
}