package ru.practicum.ewm.main.service;

import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.NewCompilationDto;
import ru.practicum.ewm.main.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.dto.CompilationFullDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto dto);
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto);
    void deleteCompilation(Long compId);
    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
    CompilationDto getCompilationById(Long compId);
    CompilationFullDto getCompilationFullById(Long compId);
}