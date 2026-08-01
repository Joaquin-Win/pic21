/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Rol
 *  com.pic21.dto.request.RegisterRequest
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 */
package com.pic21.dto.request;

import com.pic21.domain.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message="El nombre de usuario es obligatorio")
    @Size(min=3, max=50, message="El usuario debe tener entre 3 y 50 caracteres")
    private @NotBlank(message="El nombre de usuario es obligatorio") @Size(min=3, max=50, message="El usuario debe tener entre 3 y 50 caracteres") String username;
    @NotBlank(message="El nombre es obligatorio")
    private @NotBlank(message="El nombre es obligatorio") String nombre;
    @NotBlank(message="El apellido es obligatorio")
    private @NotBlank(message="El apellido es obligatorio") String apellido;
    @NotBlank(message="El email (credencial) es obligatorio")
    @Email(message="El formato del email no es v\u00e1lido")
    private @NotBlank(message="El email (credencial) es obligatorio") @Email(message="El formato del email no es v\u00e1lido") String email;
    @NotBlank(message="La contrase\u00f1a es obligatoria")
    @Size(min=6, message="La contrase\u00f1a debe tener al menos 6 caracteres")
    private @NotBlank(message="La contrase\u00f1a es obligatoria") @Size(min=6, message="La contrase\u00f1a debe tener al menos 6 caracteres") String password;
    private Rol rol;
    private String dni;
    private String correo;
    private String correoInstitucional;
    private String legajo;
    private String carrera;

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

    public String getPassword() {
        return this.password;
    }

    public Rol getRol() {
        return this.rol;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }
}

