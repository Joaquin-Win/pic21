package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de asistencia de un usuario a una reunión.
 *
 * Restricción: un usuario solo puede registrar asistencia UNA VEZ por reunión.
 * Garantizado por: unique constraint en DB + validación en el servicio.
 */
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_meeting_user",
                columnNames = {"meeting_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reunión a la que se asistió.
     * Solo se puede registrar si la reunión está ACTIVA.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /**
     * Usuario que asistió.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Timestamp automático del momento del registro.
     */
    @CreationTimestamp
    @Column(name = "registered_at", updatable = false, nullable = false)
    private LocalDateTime registeredAt;

    /** Número de legajo del estudiante (ingresado en el formulario de asistencia) */
    @Column(name = "legajo", length = 20)
    private String legajo;

    /** Carrera que el estudiante está cursando */
    @Column(name = "carrera", length = 150)
    private String carrera;
}
