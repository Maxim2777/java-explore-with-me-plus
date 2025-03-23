package ru.practicum.ewm.main.dto;

import lombok.*;
import ru.practicum.ewm.main.model.EventState;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private boolean paid;
    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Integer participantLimit;
    private boolean requestModeration;
    private EventState state;
    private LocationDto location;
    private UserShortDto initiator;
    private long confirmedRequests;
    private long views;
}