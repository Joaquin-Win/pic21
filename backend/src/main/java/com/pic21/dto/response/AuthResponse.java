/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.response.AuthResponse
 *  com.pic21.dto.response.AuthResponse$AuthResponseBuilder
 */
package com.pic21.dto.response;

import com.pic21.dto.response.AuthResponse;
import java.util.List;

public class AuthResponse {
    private String token;
    private String type;
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private List<String> roles;

    AuthResponse(String token, String type, Long id, String username, String nombre, String apellido, String email, List<String> roles) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.roles = roles;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public String getToken() {
        return this.token;
    }

    public String getType() {
        return this.type;
    }

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getApellido() {
        return this.apellido;
    }

    public String getEmail() {
        return this.email;
    }

    public List<String> getRoles() {
        return this.roles;
    }
}

