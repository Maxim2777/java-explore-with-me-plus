package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    // Найти все подборки, с фильтрацией по pinned
    List<Compilation> findAllByPinned(boolean pinned);
}