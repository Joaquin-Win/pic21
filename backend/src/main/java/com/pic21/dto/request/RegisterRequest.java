package com.pic21.dto.request;

import com.pic21.domain.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request para crear/registrar un nuevo usuario (UML v8).
 *
 * Grupo A (PerfilPersonal): dni, correo — para R01, R03, R04, R05
 * Grupo B (PerfilEstudiantil): correoInstitucional, legajo, carrera — para R02, R06
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email (credencial) es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    /** Rol asignado. Si es null, se asigna R02_ESTUDIANTE por defecto. */
    private Rol rol;

    // ── Grupo A (PerfilPersonal) ─────────────────────────────────
    private String dni;
    private String correo;

    // ── Grupo B (PerfilEstudiantil) ──────────────────────────────
    private String correoInstitucional;
    private String legajo;
    private String carrera;
}
