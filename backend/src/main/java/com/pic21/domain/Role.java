package com.pic21.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un rol del sistema.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private RoleName name;

    @Column(name = "description")
    private String description;

    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Roles disponibles en el sistema PIC21.
     * PROFESOR y AYUDANTE tienen los mismos permisos de negocio.
     */
    public enum RoleName {
        ADMIN,
        PROFESOR,
        AYUDANTE,
        ESTUDIANTE
    }
}
