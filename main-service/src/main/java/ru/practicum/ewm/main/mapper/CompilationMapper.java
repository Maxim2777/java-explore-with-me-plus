package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.model.Compilation;

public class CompilationMapper {

    // Оставляем только базовый маппинг (если он вообще ещё используется)
    public static CompilationDto toEmptyDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(null) // events больше не маппим здесь
                .build();
    }
}