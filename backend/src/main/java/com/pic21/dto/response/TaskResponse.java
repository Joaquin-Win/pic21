/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.EstadoTarea
 *  com.pic21.dto.response.TaskAssignmentResponse
 *  com.pic21.dto.response.TaskResponse
 *  com.pic21.dto.response.TaskResponse$TaskResponseBuilder
 */
package com.pic21.dto.response;
import lombok.Builder;

import com.pic21.domain.EstadoTarea;
import com.pic21.dto.response.TaskAssignmentResponse;
import com.pic21.dto.response.TaskResponse;
import java.time.LocalDateTime;
import java.util.List;

public class TaskResponse {
    private Long id;
    private Long reunionId;
    private String reunionTitulo;
    private String titulo;
    private String descripcion;
    private String link;
    private List<String> links;
    private String questionsJson;
    private EstadoTarea estado;
    private Long creadoPorId;
    private String creadoPorUsername;
    private LocalDateTime createdAt;
    private long totalAsignaciones;
    private long pendientes;
    private List<TaskAssignmentResponse> asignaciones;

    @Builder
    TaskResponse(Long id, Long reunionId, String reunionTitulo, String titulo, String descripcion, String link, List<String> links, String questionsJson, EstadoTarea estado, Long creadoPorId, String creadoPorUsername, LocalDateTime createdAt, long totalAsignaciones, long pendientes, List<TaskAssignmentResponse> asignaciones) {
        this.id = id;
        this.reunionId = reunionId;
        this.reunionTitulo = reunionTitulo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.link = link;
        this.links = links;
        this.questionsJson = questionsJson;
        this.estado = estado;
        this.creadoPorId = creadoPorId;
        this.creadoPorUsername = creadoPorUsername;
        this.createdAt = createdAt;
        this.totalAsignaciones = totalAsignaciones;
        this.pendientes = pendientes;
        this.asignaciones = asignaciones;
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

    public String getTitulo() {
        return this.titulo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public String getLink() {
        return this.link;
    }

    public List<String> getLinks() {
        return this.links;
    }

    public String getQuestionsJson() {
        return this.questionsJson;
    }

    public EstadoTarea getEstado() {
        return this.estado;
    }

    public Long getCreadoPorId() {
        return this.creadoPorId;
    }

    public String getCreadoPorUsername() {
        return this.creadoPorUsername;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public long getTotalAsignaciones() {
        return this.totalAsignaciones;
    }

    public long getPendientes() {
        return this.pendientes;
    }

    public List<TaskAssignmentResponse> getAsignaciones() {
        return this.asignaciones;
    }
}

