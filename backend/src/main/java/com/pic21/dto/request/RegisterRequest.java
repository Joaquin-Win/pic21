package com.pic21.dto.request;

import com.pic21.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request para crear/registrar un nuevo usuario.
 * Solo ADMIN puede invocar este endpoint.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    private String firstName;
    private String lastName;

    /**
     * Rol asignado al nuevo usuario.
     * Si es null, se asigna ESTUDIANTE por defecto.
     */
    private Role.RoleName role;
}
