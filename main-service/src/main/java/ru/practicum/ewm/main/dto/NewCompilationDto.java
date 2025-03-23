package ru.practicum.ewm.main.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {
    private String title;
    private boolean pinned = false;
    private Set<Long> events;
}