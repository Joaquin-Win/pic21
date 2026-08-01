/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.dto.response.MeetingResponse
 *  com.pic21.dto.response.MeetingResponse$MeetingResponseBuilder
 */
package com.pic21.dto.response;

import com.pic21.domain.EstadoReunion;
import com.pic21.dto.response.MeetingResponse;
import java.time.LocalDateTime;
import java.util.List;

public class MeetingResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private EstadoReunion estado;
    private String accessCode;
    private String recordingLink;
    private String presentacionLink;
    private String newsLink;
    private String activityLink;
    private List<String> linksExtra;
    private List<String> newsLinksExtra;
    private String pdfFileName;
    private boolean hasPdfFile;
    private String creadoPorUsername;
    private LocalDateTime createdAt;

    MeetingResponse(Long id, String titulo, String descripcion, LocalDateTime fechaInicio, EstadoReunion estado, String accessCode, String recordingLink, String presentacionLink, String newsLink, String activityLink, List<String> linksExtra, List<String> newsLinksExtra, String pdfFileName, boolean hasPdfFile, String creadoPorUsername, LocalDateTime createdAt) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.accessCode = accessCode;
        this.recordingLink = recordingLink;
        this.presentacionLink = presentacionLink;
        this.newsLink = newsLink;
        this.activityLink = activityLink;
        this.linksExtra = linksExtra;
        this.newsLinksExtra = newsLinksExtra;
        this.pdfFileName = pdfFileName;
        this.hasPdfFile = hasPdfFile;
        this.creadoPorUsername = creadoPorUsername;
        this.createdAt = createdAt;
    }

    public static MeetingResponseBuilder builder() {
        return new MeetingResponseBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public LocalDateTime getFechaInicio() {
        return this.fechaInicio;
    }

    public EstadoReunion getEstado() {
        return this.estado;
    }

    public String getAccessCode() {
        return this.accessCode;
    }

    public String getRecordingLink() {
        return this.recordingLink;
    }

    public String getPresentacionLink() {
        return this.presentacionLink;
    }

    public String getNewsLink() {
        return this.newsLink;
    }

    public String getActivityLink() {
        return this.activityLink;
    }

    public List<String> getLinksExtra() {
        return this.linksExtra;
    }

    public List<String> getNewsLinksExtra() {
        return this.newsLinksExtra;
    }

    public String getPdfFileName() {
        return this.pdfFileName;
    }

    public boolean isHasPdfFile() {
        return this.hasPdfFile;
    }

    public String getCreadoPorUsername() {
        return this.creadoPorUsername;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}

