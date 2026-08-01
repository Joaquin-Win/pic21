/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.EstadoTarea
 *  com.pic21.dto.response.TaskAssignmentResponse
 *  com.pic21.dto.response.TaskAssignmentResponse$TaskAssignmentResponseBuilder
 */
package com.pic21.dto.response;
import lombok.Builder;

import com.pic21.domain.EstadoTarea;
import com.pic21.dto.response.TaskAssignmentResponse;
import java.time.LocalDateTime;
import java.util.List;

public class TaskAssignmentResponse {
    private Long id;
    private Long tareaId;
    private String tituloTarea;
    private String descripcionTarea;
    private String linkTarea;
    private List<String> linksTarea;
    private Long reunionId;
    private String reunionTitulo;
    private Long usuarioId;
    private String username;
    private String nombre;
    private String apellido;
    private EstadoTarea estado;
    private Integer score;
    private int intentos;
    private String questionsJson;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaCompletado;

    @Builder
    TaskAssignmentResponse(Long id, Long tareaId, String tituloTarea, String descripcionTarea, String linkTarea, List<String> linksTarea, Long reunionId, String reunionTitulo, Long usuarioId, String username, String nombre, String apellido, EstadoTarea estado, Integer score, int intentos, String questionsJson, LocalDateTime fechaAsignacion, LocalDateTime fechaCompletado) {
        this.id = id;
        this.tareaId = tareaId;
        this.tituloTarea = tituloTarea;
        this.descripcionTarea = descripcionTarea;
        this.linkTarea = linkTarea;
        this.linksTarea = linksTarea;
        this.reunionId = reunionId;
        this.reunionTitulo = reunionTitulo;
        this.usuarioId = usuarioId;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.estado = estado;
        this.score = score;
        this.intentos = intentos;
        this.questionsJson = questionsJson;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaCompletado = fechaCompletado;
    }
    public Long getId() {
        return this.id;
    }

    public Long getTareaId() {
        return this.tareaId;
    }

    public String getTituloTarea() {
        return this.tituloTarea;
    }

    public String getDescripcionTarea() {
        return this.descripcionTarea;
    }

    public String getLinkTarea() {
        return this.linkTarea;
    }

    public List<String> getLinksTarea() {
        return this.linksTarea;
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

    public EstadoTarea getEstado() {
        return this.estado;
    }

    public Integer getScore() {
        return this.score;
    }

    public int getIntentos() {
        return this.intentos;
    }

    public String getQuestionsJson() {
        return this.questionsJson;
    }

    public LocalDateTime getFechaAsignacion() {
        return this.fechaAsignacion;
    }

    public LocalDateTime getFechaCompletado() {
        return this.fechaCompletado;
    }
}

