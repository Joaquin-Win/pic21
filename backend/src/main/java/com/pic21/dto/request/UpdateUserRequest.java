package com.pic21.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Request para actualizar perfil de usuario.
 */
@Getter
@Setter
public class UpdateUserRequest {

    private String username;
    private String nombre;
    private String apellido;

    // ── Grupo A ──────────────────────────────────────────────────
    private String dni;
    private String correo;

    // ── Grupo B ──────────────────────────────────────────────────
    private String correoInstitucional;
    private String legajo;
    private String carrera;
}
