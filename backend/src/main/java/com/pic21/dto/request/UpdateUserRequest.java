/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.request.UpdateUserRequest
 */
package com.pic21.dto.request;

public class UpdateUserRequest {
    private String username;
    private String nombre;
    private String apellido;
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

