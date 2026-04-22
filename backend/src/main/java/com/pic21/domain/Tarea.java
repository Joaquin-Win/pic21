package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tarea (UML v8).
 *
 * Composición 1:1 con Reunion (hereda id de Reunión via @MapsId).
 * estado usa EstadoTarea { PENDIENTE, COMPLETADA, BLOQUEADA }.
 *
 * Se conservan campos funcionales: link, questionsJson, createdBy
 * (son lógica de negocio, no UML puro — no se destruye).
 */
@Entity
@Table(name = "tareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    /**
     * Hereda el id de la Reunion (composición 1:1 por @MapsId).
     * Una Reunion tiene exactamente una Tarea.
     */
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "reunion_id")
    private Reunion reunion;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    // ── Campos funcionales conservados ──────────────────────────

    @Column(length = 500)
    private String link;

    /** JSON array con links de apoyo adicionales. */
    @Column(name = "links_extra_json", columnDefinition = "TEXT")
    @Builder.Default
    private String linksExtraJson = "[]";

    /** JSON array con las preguntas del quiz multiple choice */
    @Column(name = "questions_json", columnDefinition = "TEXT")
    private String questionsJson;

    /**
     * Usuario creador de la tarea (PROFESOR o ADMIN).
     * EAGER para evitar LazyInitializationException al mapear.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    /** Asignaciones individuales. Cascade ALL elimina assignments al borrar tarea. */
    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AsignacionTarea> asignaciones = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
