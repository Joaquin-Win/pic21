package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AsignacionTarea (UML v8).
 *
 * fechaAsignacion: timestamp automático de creación.
 * fechaCompletado: se setea cuando el estado pasa a COMPLETADA.
 * ManyToOne → Usuario (muchos a uno).
 * ManyToOne → Tarea (muchos a uno).
 *
 * Se conservan: status (EstadoTarea), score, attempts, questionsJson
 * (lógica de quiz existente — no se destruye).
 */
@Entity
@Table(
        name = "asignaciones_tarea",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tarea_id", "usuario_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", updatable = false, nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_completado")
    private LocalDateTime fechaCompletado;

    // ── Campos funcionales conservados (quiz) ────────────────────

    /** Porcentaje obtenido en el último intento del quiz */
    @Column(name = "score")
    private Integer score;

    /** Cantidad de intentos realizados */
    @Column(name = "attempts")
    @Builder.Default
    private int attempts = 0;
}
