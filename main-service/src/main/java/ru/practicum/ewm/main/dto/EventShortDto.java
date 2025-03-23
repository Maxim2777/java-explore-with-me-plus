package ru.practicum.ewm.main.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private boolean paid;
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private long confirmedRequests;
    private long views;
}