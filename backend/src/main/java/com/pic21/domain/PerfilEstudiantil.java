package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Perfil estudiantil para roles Grupo B (UML v8):
 * R02_ESTUDIANTE, R06_AYUDANTE.
 *
 * Composición 0:1 con Usuario — embebido en tabla usuarios.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilEstudiantil {

    @Column(name = "correo_institucional", length = 150)
    private String correoInstitucional;

    @Column(name = "legajo", length = 20)
    private String legajo;

    @Column(name = "carrera", length = 150)
    private String carrera;
}
