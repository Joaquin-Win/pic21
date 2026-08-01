/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Reunion$ReunionBuilder
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.Basic
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.Lob
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;

import com.pic21.domain.EstadoReunion;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/*
 * Exception performing whole class analysis ignored.
 */
@Entity
@Table(name="reuniones")
public class Reunion {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=200)
    private String titulo;
    @Column(columnDefinition="TEXT")
    private String descripcion;
    @Column(name="fecha_inicio", nullable=false)
    private LocalDateTime fechaInicio;
    @Enumerated(value=EnumType.STRING)
    @Column(nullable=false, length=20)
    private EstadoReunion estado;
    @Column(name="access_code", length=1000)
    private String accessCode;
    @Column(name="recording_link", length=1000)
    private String recordingLink;
    @Column(name="presentacion_link", length=1000)
    private String presentacionLink;
    @Column(name="news_link", length=1000)
    private String newsLink;
    @Column(name="activity_link", length=1000)
    private String activityLink;
    @Column(name="links_extra", columnDefinition="TEXT")
    private String linksExtraJson;
    @Column(name="news_links_extra_json", columnDefinition="TEXT")
    private String newsLinksExtraJson;
    @Lob
    @Basic(fetch=FetchType.EAGER)
    @Column(name="pdf_file_data")
    private byte[] pdfFileData;
    @Column(name="pdf_file_name")
    private String pdfFileName;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="creado_por", nullable=false)
    private Usuario creadoPor;
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    private static EstadoReunion $default$estado() {
        return EstadoReunion.NO_INICIADA;
    }

    private static String $default$linksExtraJson() {
        return "[]";
    }

    private static String $default$newsLinksExtraJson() {
        return "[]";
    }

    public static ReunionBuilder builder() {
        return new ReunionBuilder();
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

    public String getLinksExtraJson() {
        return this.linksExtraJson;
    }

    public String getNewsLinksExtraJson() {
        return this.newsLinksExtraJson;
    }

    public byte[] getPdfFileData() {
        return this.pdfFileData;
    }

    public String getPdfFileName() {
        return this.pdfFileName;
    }

    public Usuario getCreadoPor() {
        return this.creadoPor;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setEstado(EstadoReunion estado) {
        this.estado = estado;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public void setRecordingLink(String recordingLink) {
        this.recordingLink = recordingLink;
    }

    public void setPresentacionLink(String presentacionLink) {
        this.presentacionLink = presentacionLink;
    }

    public void setNewsLink(String newsLink) {
        this.newsLink = newsLink;
    }

    public void setActivityLink(String activityLink) {
        this.activityLink = activityLink;
    }

    public void setLinksExtraJson(String linksExtraJson) {
        this.linksExtraJson = linksExtraJson;
    }

    public void setNewsLinksExtraJson(String newsLinksExtraJson) {
        this.newsLinksExtraJson = newsLinksExtraJson;
    }

    public void setPdfFileData(byte[] pdfFileData) {
        this.pdfFileData = pdfFileData;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Reunion() {
        this.estado = Reunion.$default$estado();
        this.linksExtraJson = Reunion.$default$linksExtraJson();
        this.newsLinksExtraJson = Reunion.$default$newsLinksExtraJson();
    }

    public Reunion(Long id, String titulo, String descripcion, LocalDateTime fechaInicio, EstadoReunion estado, String accessCode, String recordingLink, String presentacionLink, String newsLink, String activityLink, String linksExtraJson, String newsLinksExtraJson, byte[] pdfFileData, String pdfFileName, Usuario creadoPor, LocalDateTime createdAt) {
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
        this.linksExtraJson = linksExtraJson;
        this.newsLinksExtraJson = newsLinksExtraJson;
        this.pdfFileData = pdfFileData;
        this.pdfFileName = pdfFileName;
        this.creadoPor = creadoPor;
        this.createdAt = createdAt;
    }
}

