package ru.practicum.ewm.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.service.CompilationService;
import ru.practicum.ewm.main.dto.CompilationFullDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationController {

    public static final String COMP_ID = "comp-id";
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{" + COMP_ID + "}")
    public CompilationDto getCompilationById(@PathVariable(COMP_ID) Long compId) {
        return compilationService.getCompilationById(compId);
    }
}