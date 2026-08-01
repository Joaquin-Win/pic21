/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.response.AttendanceResponse
 *  com.pic21.dto.response.AttendanceResponse$AttendanceResponseBuilder
 */
package com.pic21.dto.response;

import com.pic21.dto.response.AttendanceResponse;
import java.time.LocalDateTime;

public class AttendanceResponse {
    private Long id;
    private Long reunionId;
    private String reunionTitulo;
    private Long usuarioId;
    private String username;
    private String nombre;
    private String apellido;
    private String email;
    private boolean presente;
    private LocalDateTime fechaRegistro;

    AttendanceResponse(Long id, Long reunionId, String reunionTitulo, Long usuarioId, String username, String nombre, String apellido, String email, boolean presente, LocalDateTime fechaRegistro) {
        this.id = id;
        this.reunionId = reunionId;
        this.reunionTitulo = reunionTitulo;
        this.usuarioId = usuarioId;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.presente = presente;
        this.fechaRegistro = fechaRegistro;
    }

    public static AttendanceResponseBuilder builder() {
        return new AttendanceResponseBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getReunionId() {
        return this.reunionId;
    }

    public String getReunionTitulo() {
        return this.reunionTitulo;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
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

    public boolean isPresente() {
        return this.presente;
    }

    public LocalDateTime getFechaRegistro() {
        return this.fechaRegistro;
    }
}

