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
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;

    @NotBlank(message = "Annotation must not be blank")
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
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