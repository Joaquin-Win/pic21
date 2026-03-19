package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
     * Usuario creador de la tarea (PROFESOR o ADMIN).
     * EAGER para evitar LazyInitializationException durante el mapeo.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * [LEGACY] Asignación directa anterior al modelo de TaskAssignment.
     * Mantenido para no romper la columna en BD. Las nuevas tareas usan TaskAssignment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_to", nullable = true)
    private User legacyAssignedTo;

    /**
     * [LEGACY] Estado de la asignación anterior al modelo de TaskAssignment.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true, length = 20)
    private TaskStatus legacyStatus;

    /** Asignaciones individuales (nuevo modelo: una por ESTUDIANTE/AYUDANTE ausente). */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TaskAssignment> assignments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
