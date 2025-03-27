package ru.practicum.ewm.main.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.CompilationRequestDto;
import ru.practicum.ewm.main.mapper.CompilationMapper;
import ru.practicum.ewm.main.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
@Slf4j
public class PublicCompilationController {

    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        CompilationRequestDto requestDto = compilationMapper.toCompilationRequestDto(pinned, from, size);

        log.info("Получен запрос на получение компиляций с pinned = {}, from = {}, size = {}", pinned, from, size);
        List<CompilationDto> compilations = compilationService.getCompilations(requestDto);
        log.info("Возвращаем {} компиляций", compilations.size());
        return compilations;
    }

    @GetMapping("/{comp-id}")
    public CompilationDto getCompilationById(@PathVariable("comp-id") Long compId) {
        log.info("Received request to get compilation with ID: {}", compId);
        CompilationDto compilationDto = compilationService.getCompilationById(compId);
        log.info("Returning compilation with ID: {}", compId);
        return compilationDto;
    }
}