package ru.practicum.ewm.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.mapper.CompilationFullMapper;
import ru.practicum.ewm.main.mapper.CompilationMapper;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.repository.CompilationRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.CategoryService;
import ru.practicum.ewm.main.service.CompilationService;
import ru.practicum.ewm.main.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;     // добавлено
    private final UserService userService;             // добавлено

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Set<Event> events = dto.getEvents() == null ? Set.of()
                : new HashSet<>(eventRepository.findAllById(dto.getEvents()));
        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.isPinned())
                .events(events)
                .build();
        return CompilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NoSuchElementException("Compilation not found"));

        if (dto.getTitle() != null) compilation.setTitle(dto.getTitle());
        if (dto.getPinned() != null) compilation.setPinned(dto.getPinned());
        if (dto.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            compilation.setEvents(events);
        }

        return CompilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> result = (pinned != null)
                ? compilationRepository.findAllByPinned(pinned)
                : compilationRepository.findAll(PageRequest.of(from / size, size)).getContent();

        return result.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NoSuchElementException("Compilation not found"));
        return CompilationMapper.toDto(compilation);
    }

    @Override
    public CompilationFullDto getCompilationFullById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new IllegalArgumentException("Compilation not found"));

        // !!! Временно один и тот же инициатор и категория на все события
        Event example = compilation.getEvents().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Compilation has no events"));

        CategoryDto category = categoryService.getById(example.getCategoryId());
        UserShortDto user = userService.getShortById(example.getInitiatorId());

        return CompilationFullMapper.toDto(compilation, category, user);
    }
}