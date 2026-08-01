/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.AsignacionTarea
 *  com.pic21.domain.AsignacionTarea$AsignacionTareaBuilder
 *  com.pic21.domain.EstadoTarea
 *  com.pic21.domain.Tarea
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;
import lombok.Builder;

import com.pic21.domain.AsignacionTarea;
import com.pic21.domain.EstadoTarea;
import com.pic21.domain.Tarea;
import com.pic21.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/*
 * Exception performing whole class analysis ignored.
 */
@Entity
@Table(name="asignaciones_tarea", uniqueConstraints={@UniqueConstraint(columnNames={"tarea_id", "usuario_id"})})
public class AsignacionTarea {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tarea_id", nullable=false)
    private Tarea tarea;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="usuario_id", nullable=false)
    private Usuario usuario;
    @Enumerated(value=EnumType.STRING)
    @Column(nullable=false, length=20)
    private EstadoTarea estado;
    @CreationTimestamp
    @Column(name="fecha_asignacion", updatable=false, nullable=false)
    private LocalDateTime fechaAsignacion;
    @Column(name="fecha_completado")
    private LocalDateTime fechaCompletado;
    @Column(name="score")
    private Integer score;
    @Column(name="attempts")
    private int attempts;

    private static EstadoTarea $default$estado() {
        return EstadoTarea.PENDIENTE;
    }

    private static int $default$attempts() {
        return 0;
    }
    public Long getId() {
        return this.id;
    }

    public Tarea getTarea() {
        return this.tarea;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public EstadoTarea getEstado() {
        return this.estado;
    }

    public LocalDateTime getFechaAsignacion() {
        return this.fechaAsignacion;
    }

    public LocalDateTime getFechaCompletado() {
        return this.fechaCompletado;
    }

    public Integer getScore() {
        return this.score;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public void setFechaCompletado(LocalDateTime fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public AsignacionTarea() {
        this.estado = AsignacionTarea.$default$estado();
        this.attempts = AsignacionTarea.$default$attempts();
    }

    @Builder
    public AsignacionTarea(Long id, Tarea tarea, Usuario usuario, EstadoTarea estado, LocalDateTime fechaAsignacion, LocalDateTime fechaCompletado, Integer score, int attempts) {
        this.id = id;
        this.tarea = tarea;
        this.usuario = usuario;
        this.estado = estado;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaCompletado = fechaCompletado;
        this.score = score;
        this.attempts = attempts;
    }
}

