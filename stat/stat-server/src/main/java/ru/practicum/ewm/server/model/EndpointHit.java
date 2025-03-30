package ru.practicum.ewm.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с приложением
    @ManyToOne(optional = false)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    // Связь с URI
    @ManyToOne(optional = false)
    @JoinColumn(name = "uri_id", nullable = false)
    private Uri uri;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}