package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tarea general en PIC21.
 * Una tarea puede tener múltiples TaskAssignment (una por ESTUDIANTE o AYUDANTE ausente).
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String link;

    /** JSON array con las preguntas del quiz multiple choice */
    @Column(name = "questions_json", columnDefinition = "TEXT")
    private String questionsJson;

    /**
     * Usuario creador de la tarea (PROFESOR o ADMIN).
     * EAGER para evitar LazyInitializationException al mappear.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * [LEGACY] Columna assigned_to mantenida en BD (NOT NULL → nullable via ALTER TABLE).
     * Las nuevas tareas usan TaskAssignment en su lugar.
     * Hibernate ddl-auto=update ejecuta ALTER TABLE tasks ALTER COLUMN assigned_to DROP NOT NULL.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "assigned_to", nullable = true)
    private User legacyAssignedTo;

    /**
     * [LEGACY] Columna status — se mantiene nullable para backward compat.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true, length = 20)
    private TaskStatus legacyStatus;

    /** Asignaciones individuales (nuevo modelo). Cascade ALL elimina assignments al borrar tarea. */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TaskAssignment> assignments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
