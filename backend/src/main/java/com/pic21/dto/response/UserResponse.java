package com.pic21.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos públicos de un usuario.
 * passwordHash se muestra SOLO en el panel de admin.
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private String passwordHash;
    private LocalDateTime createdAt;
    private List<String> roles;
}

