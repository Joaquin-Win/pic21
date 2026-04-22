package com.pic21.dto.response;

import com.pic21.domain.Rol;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos públicos de un usuario (UML v8).
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private boolean activo;
    private LocalDateTime fechaRegistro;
    private List<String> roles;

    // Credencial
    private String email;

    // PerfilPersonal (Grupo A)
    private String dni;
    private String correo;

    // PerfilEstudiantil (Grupo B)
    private String correoInstitucional;
    private String legajo;
    private String carrera;

    // Admin only
    private String passwordHash;
}
