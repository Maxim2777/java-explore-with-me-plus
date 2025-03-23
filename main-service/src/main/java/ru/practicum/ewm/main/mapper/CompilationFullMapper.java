package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CompilationFullDto;
import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.dto.UserShortDto;
import ru.practicum.ewm.main.model.Compilation;

import java.util.stream.Collectors;

public class CompilationFullMapper {

    public static CompilationFullDto toDto(Compilation compilation,
                                           CategoryDto category,
                                           UserShortDto initiator) {
        return CompilationFullDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(
                        compilation.getEvents().stream()
                                .map(event -> EventMapper.toShortDto(
                                        event,
                                        category,
                                        initiator,
                                        0, 0
                                ))
                                .collect(Collectors.toSet())
                )
                .build();
    }
}
