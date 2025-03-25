package ru.practicum.ewm.main.dto;

import lombok.*;

import java.util.Set;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {

    @NotBlank(message = "Title is required and must not be blank")
    @Size(min = 1, max = 50, message = "Title must be from 1 to 50 characters")
    private String title;

    private boolean pinned = false;

    private Set<Long> events;
}