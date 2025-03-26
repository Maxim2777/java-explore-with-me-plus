package ru.practicum.ewm.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.NewCompilationDto;
import ru.practicum.ewm.main.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.service.CompilationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    public static final String COMP_ID = "comp-id";
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        return compilationService.create(newCompilationDto);
    }

    @DeleteMapping("/{" + COMP_ID + "}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable(COMP_ID) Long compId) {
        compilationService.deleteById(compId);
    }

    @PatchMapping("/{" + COMP_ID + "}")
    public CompilationDto update(@PathVariable(COMP_ID) Long compId,
                                 @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        return compilationService.update(compId, updateCompilationRequest);
    }
}