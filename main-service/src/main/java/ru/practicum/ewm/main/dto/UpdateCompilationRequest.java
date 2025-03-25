package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    @Size(max = 50, message = "Title must be at most 50 characters")
    private String title;

    private Boolean pinned;

    private Set<Long> events;
}