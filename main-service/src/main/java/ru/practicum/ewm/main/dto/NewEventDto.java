package ru.practicum.ewm.main.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private LocationDto location;
    private String eventDate;
    private boolean paid = false;
    private int participantLimit = 0;
    private boolean requestModeration = true;
}