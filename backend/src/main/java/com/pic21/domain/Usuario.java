package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Entidad principal del sistema PIC21 (UML v8).
 *
 * Composiciones:
 *   - Credencial  1:1  (cascade ALL, orphanRemoval)
 *   - PerfilPersonal  0:1 (@Embedded, nullable — Grupo A)
 *   - PerfilEstudiantil 0:1 (@Embedded, nullable — Grupo B)
 *
 * Roles almacenados como @ElementCollection de enum Rol.
 * Se mantiene username para autenticación Spring Security.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    /**
     * Username para login — no está en UML pero es necesario para Spring Security.
     * Se puede usar el email como username también.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "rol", length = 20)
    private Set<Rol> roles = EnumSet.noneOf(Rol.class);

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    // ── Composición 1:1 con Credencial ──────────────────────────
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "credencial_id", nullable = false, unique = true)
    private Credencial credencial;

    // ── Composición 0:1 con PerfilPersonal (Grupo A) ────────────
    @Embedded
    private PerfilPersonal perfilPersonal;

    // ── Composición 0:1 con PerfilEstudiantil (Grupo B) ─────────
    @Embedded
    private PerfilEstudiantil perfilEstudiantil;

    // ── Helpers de negocio ───────────────────────────────────────

    /** Retorna true si el usuario pertenece al Grupo A (PerfilPersonal). */
    public boolean esGrupoA() {
        return roles.contains(Rol.R01_PROFESOR)
                || roles.contains(Rol.R03_EGRESADO)
                || roles.contains(Rol.R04_ADMIN)
                || roles.contains(Rol.R05_DIRECTOR);
    }

    /** Retorna true si el usuario pertenece al Grupo B (PerfilEstudiantil). */
    public boolean esGrupoB() {
        return roles.contains(Rol.R02_ESTUDIANTE)
                || roles.contains(Rol.R06_AYUDANTE);
    }
}
