package com.pic21.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Response devuelta tras un login exitoso.
 * Incluye el JWT y los datos básicos del usuario autenticado.
 */
@Getter
@Builder
public class AuthResponse {

    private String token;
    private String type;       // "Bearer"
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private List<String> roles;
}
