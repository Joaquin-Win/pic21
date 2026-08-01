/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.response.UserResponse
 *  com.pic21.dto.response.UserResponse$UserResponseBuilder
 */
package com.pic21.dto.response;

import com.pic21.dto.response.UserResponse;
import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private boolean activo;
    private LocalDateTime fechaRegistro;
    private List<String> roles;
    private String email;
    private String dni;
    private String correo;
    private String correoInstitucional;
    private String legajo;
    private String carrera;
    private String passwordHash;

    UserResponse(Long id, String username, String nombre, String apellido, boolean activo, LocalDateTime fechaRegistro, List<String> roles, String email, String dni, String correo, String correoInstitucional, String legajo, String carrera, String passwordHash) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.roles = roles;
        this.email = email;
        this.dni = dni;
        this.correo = correo;
        this.correoInstitucional = correoInstitucional;
        this.legajo = legajo;
        this.carrera = carrera;
        this.passwordHash = passwordHash;
    }

    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
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

    public boolean isActivo() {
        return this.activo;
    }

    public LocalDateTime getFechaRegistro() {
        return this.fechaRegistro;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public String getEmail() {
        return this.email;
    }

    public String getDni() {
        return this.dni;
    }

    public String getCorreo() {
        return this.correo;
    }

    public String getCorreoInstitucional() {
        return this.correoInstitucional;
    }

    public String getLegajo() {
        return this.legajo;
    }

    public String getCarrera() {
        return this.carrera;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }
}

