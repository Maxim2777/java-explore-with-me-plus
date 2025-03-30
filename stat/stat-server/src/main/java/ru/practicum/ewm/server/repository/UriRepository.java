package ru.practicum.ewm.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.server.model.Uri;

import java.util.Optional;

public interface UriRepository extends JpaRepository<Uri, Long> {
    Optional<Uri> findByUri(String uri);
}