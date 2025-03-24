package ru.practicum.ewm.main.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Annotation must not be blank")
    private String annotation;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotNull(message = "Category is required")
    private Long category;

    @NotNull(message = "Location is required")
    private LocationDto location;

    @NotBlank(message = "Event date is required")
    private String eventDate;

    private boolean paid = false;

    @PositiveOrZero(message = "Participant limit must be 0 or greater")
    private int participantLimit = 0;

    private boolean requestModeration = true;
}