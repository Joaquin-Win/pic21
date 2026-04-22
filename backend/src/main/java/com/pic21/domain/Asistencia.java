package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Asistencia (UML v8).
 *
 * fechaRegistro: timestamp automático.
 * presente: boolean — true si asistió.
 * ManyToOne → Usuario (muchos a uno).
 * ManyToOne → Reunion (muchos a uno).
 *
 * Restricción: un usuario solo puede registrar asistencia UNA VEZ por reunión.
 */
@Entity
@Table(
        name = "asistencias",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_asistencia_reunion_usuario",
                columnNames = {"reunion_id", "usuario_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", nullable = false)
    private Reunion reunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false, nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    @Builder.Default
    private boolean presente = true;
}
