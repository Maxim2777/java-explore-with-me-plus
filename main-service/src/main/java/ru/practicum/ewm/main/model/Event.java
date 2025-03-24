package ru.practicum.ewm.main.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT") // добавлено
    private String title;

    @Column(columnDefinition = "TEXT") // добавлено
    private String annotation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    private boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    @Embedded
    private Location location;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Column(name = "category_id")
    private Long categoryId;
}