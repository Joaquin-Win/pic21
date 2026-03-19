package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Asignación individual de una tarea a un usuario.
 * Una Task general puede tener múltiples TaskAssignment (uno por usuario ausente).
 */
@Entity
@Table(name = "task_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tarea general a la que pertenece esta asignación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Usuario al que se asignó la tarea (ESTUDIANTE o AYUDANTE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User assignedTo;

    /** Estado individual de la asignación para este usuario. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
