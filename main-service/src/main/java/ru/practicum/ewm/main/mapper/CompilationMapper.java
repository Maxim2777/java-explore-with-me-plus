package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;

import java.util.stream.Collectors;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(
                        compilation.getEvents().stream()
                                .map(Event::getId)  // ← теперь просто ID
                                .collect(Collectors.toSet())
                )
                .build();
    }
}