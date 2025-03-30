package ru.practicum.ewm.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.CompilationMapper;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.repository.CompilationRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.service.CompilationService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    final CompilationRepository compilationRepository;
    final EventRepository eventRepository;
    final CompilationMapper compilationMapper;
    final EventMapper eventMapper;

    @Override
    public List<CompilationDto> getCompilations(CompilationRequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getFrom() / requestDto.getSize(), requestDto.getSize());

        Page<Compilation> compilationsPage;
        if (requestDto.getPinned() != null) {
            compilationsPage = compilationRepository.findAllByPinned(requestDto.getPinned(), pageable);
        } else {
            compilationsPage = compilationRepository.findAll(pageable);
        }

        if (compilationsPage.isEmpty()) {
            return List.of();
        }

        List<CompilationDto> compilationDtos = compilationsPage.getContent().stream()
                .map(compilation -> {
                    List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                            .map(eventMapper::toEventShortDtoFromEvent)
                            .collect(Collectors.toList());

                    return compilationMapper.toCompilationDtoFromCompilation(compilation, eventShortDtos);
                })
                .collect(Collectors.toList());

        return compilationDtos;
    }



    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d not found", compId)));

        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(eventMapper::toEventShortDtoFromEvent)
                .collect(Collectors.toList());

        CompilationDto compilationDto = compilationMapper.toCompilationDtoFromCompilation(compilation, eventShortDtos);

        return compilationDto;
    }


    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Optional<Compilation> existingCompilation = compilationRepository.findByTitle(newCompilationDto.getTitle());
        if (existingCompilation.isPresent()) {
            throw new ConflictException("Compilation with the title '" + newCompilationDto.getTitle() + "' already exists.");
        }
        Set<Event> events = new HashSet<>();

        if (newCompilationDto.getEvents() != null) {
            events.addAll(eventRepository.findAllById(newCompilationDto.getEvents()));
        }

        Compilation compilation = compilationMapper.toCompilationFromNewCompilationDto(newCompilationDto, events);

        compilation = compilationRepository.save(compilation);

        List<EventShortDto> eventShortDtos = compilation.getEvents().stream().map(eventMapper::toEventShortDtoFromEvent).toList();

        CompilationDto compilationDto = compilationMapper.toCompilationDtoFromCompilation(compilation, eventShortDtos);

        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteById(Long compId) {

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(
                    String.format("Compilation with id=%d not found", compId));
        }
        compilationRepository.deleteById(compId);

    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d not found", compId)));
        StringBuilder updatedFieldsLog = new StringBuilder();

        if (updateCompilationRequest.getTitle() != null && !updateCompilationRequest.getTitle().equals(compilation.getTitle())) {
            Optional<Compilation> existingCompilation = compilationRepository.findByTitle(updateCompilationRequest.getTitle());
            if (existingCompilation.isPresent()) {
                throw new ConflictException("Compilation with the title '" + updateCompilationRequest.getTitle() + "' already exists.");
            }
            compilation.setTitle(updateCompilationRequest.getTitle());
            updatedFieldsLog.append("Title|");
        }

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationRequest.getEvents()));
            compilation.setEvents(events);
            updatedFieldsLog.append("Events|");
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
            updatedFieldsLog.append("Pinned|");
        }

        String updatedFields = updatedFieldsLog.toString().replaceAll("\\|$", "").replace("|", ", ");
        compilationRepository.save(compilation);

        CompilationDto compilationDto = compilationMapper.toCompilationDtoFromCompilation(compilation, compilation.getEvents().stream()
                .map(eventMapper::toEventShortDtoFromEvent)
                .toList());

        return compilationDto;
    }

}