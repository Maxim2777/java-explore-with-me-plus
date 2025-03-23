package ru.practicum.ewm.main.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {
    private String title;
    private Boolean pinned;
    private Set<Long> events;
}