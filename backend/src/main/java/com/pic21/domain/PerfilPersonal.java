package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Perfil personal para roles Grupo A (UML v8):
 * R01_PROFESOR, R03_EGRESADO, R04_ADMIN, R05_DIRECTOR.
 *
 * Composición 0:1 con Usuario — embebido en tabla usuarios.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilPersonal {

    @Column(name = "dni", length = 8)
    private String dni;

    @Column(name = "correo", length = 150)
    private String correo;
}
