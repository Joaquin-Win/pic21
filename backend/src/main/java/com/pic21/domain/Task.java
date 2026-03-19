package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una tarea asignada a un estudiante en el contexto de una reunión.
 *
 * Regla de negocio clave:
 *   Solo los estudiantes que NO registraron asistencia en la reunión
 *   pueden recibir tareas de esa reunión.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reunión a la que está asociada la tarea.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /**
     * Título descriptivo de la tarea.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Descripción detallada de lo que debe hacer el estudiante.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL o enlace de referencia para la tarea (documento, formulario, etc.).
     */
    @Column(length = 500)
    private String link;

    /**
     * Estudiante al que está asignada la tarea.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    /**
     * Usuario que creó la tarea (PROFESOR o AYUDANTE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Estado actual de la tarea.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
