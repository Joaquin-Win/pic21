/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.AsignacionTarea
 *  com.pic21.domain.EstadoTarea
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Tarea
 *  com.pic21.domain.Tarea$TareaBuilder
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.CascadeType
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.MapsId
 *  jakarta.persistence.OneToMany
 *  jakarta.persistence.OneToOne
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;
import lombok.Builder;

import com.pic21.domain.AsignacionTarea;
import com.pic21.domain.EstadoTarea;
import com.pic21.domain.Reunion;
import com.pic21.domain.Tarea;
import com.pic21.domain.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

/*
 * Exception performing whole class analysis ignored.
 */
@Entity
@Table(name="tareas")
public class Tarea {
    @Id
    private Long id;
    @OneToOne(fetch=FetchType.LAZY)
    @MapsId
    @JoinColumn(name="reunion_id")
    private Reunion reunion;
    @Column(nullable=false, length=200)
    private String titulo;
    @Column(columnDefinition="TEXT")
    private String descripcion;
    @Enumerated(value=EnumType.STRING)
    @Column(nullable=false, length=20)
    private EstadoTarea estado;
    @Column(length=500)
    private String link;
    @Column(name="links_extra_json", columnDefinition="TEXT")
    private String linksExtraJson;
    @Column(name="questions_json", columnDefinition="TEXT")
    private String questionsJson;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="creado_por", nullable=false)
    private Usuario creadoPor;
    @OneToMany(mappedBy="tarea", cascade={CascadeType.ALL}, orphanRemoval=true, fetch=FetchType.LAZY)
    private List<AsignacionTarea> asignaciones;
    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    private static EstadoTarea $default$estado() {
        return EstadoTarea.PENDIENTE;
    }

    private static String $default$linksExtraJson() {
        return "[]";
    }

    private static List<AsignacionTarea> $default$asignaciones() {
        return new ArrayList<AsignacionTarea>();
    }
    public Long getId() {
        return this.id;
    }

    public Reunion getReunion() {
        return this.reunion;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public EstadoTarea getEstado() {
        return this.estado;
    }

    public String getLink() {
        return this.link;
    }

    public String getLinksExtraJson() {
        return this.linksExtraJson;
    }

    public String getQuestionsJson() {
        return this.questionsJson;
    }

    public Usuario getCreadoPor() {
        return this.creadoPor;
    }

    public List<AsignacionTarea> getAsignaciones() {
        return this.asignaciones;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReunion(Reunion reunion) {
        this.reunion = reunion;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setLinksExtraJson(String linksExtraJson) {
        this.linksExtraJson = linksExtraJson;
    }

    public void setQuestionsJson(String questionsJson) {
        this.questionsJson = questionsJson;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
    }

    public void setAsignaciones(List<AsignacionTarea> asignaciones) {
        this.asignaciones = asignaciones;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Tarea() {
        this.estado = Tarea.$default$estado();
        this.linksExtraJson = Tarea.$default$linksExtraJson();
        this.asignaciones = Tarea.$default$asignaciones();
    }

    @Builder
    public Tarea(Long id, Reunion reunion, String titulo, String descripcion, EstadoTarea estado, String link, String linksExtraJson, String questionsJson, Usuario creadoPor, List<AsignacionTarea> asignaciones, LocalDateTime createdAt) {
        this.id = id;
        this.reunion = reunion;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.link = link;
        this.linksExtraJson = linksExtraJson;
        this.questionsJson = questionsJson;
        this.creadoPor = creadoPor;
        this.asignaciones = asignaciones;
        this.createdAt = createdAt;
    }
}

