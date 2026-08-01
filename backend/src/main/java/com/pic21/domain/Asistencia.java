/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Asistencia
 *  com.pic21.domain.Asistencia$AsistenciaBuilder
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
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

import com.pic21.domain.Asistencia;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name="asistencias", uniqueConstraints={@UniqueConstraint(name="uk_asistencia_reunion_usuario", columnNames={"reunion_id", "usuario_id"})})
public class Asistencia {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="reunion_id", nullable=false)
    private Reunion reunion;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="usuario_id", nullable=false)
    private Usuario usuario;
    @CreationTimestamp
    @Column(name="fecha_registro", updatable=false, nullable=false)
    private LocalDateTime fechaRegistro;
    @Column(nullable=false)
    private boolean presente;

    private static boolean $default$presente() {
        return true;
    }
    public Long getId() {
        return this.id;
    }

    public Reunion getReunion() {
        return this.reunion;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public LocalDateTime getFechaRegistro() {
        return this.fechaRegistro;
    }

    public boolean isPresente() {
        return this.presente;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReunion(Reunion reunion) {
        this.reunion = reunion;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setPresente(boolean presente) {
        this.presente = presente;
    }

    public Asistencia() {
        this.presente = Asistencia.$default$presente();
    }

    @Builder
    public Asistencia(Long id, Reunion reunion, Usuario usuario, LocalDateTime fechaRegistro, boolean presente) {
        this.id = id;
        this.reunion = reunion;
        this.usuario = usuario;
        this.fechaRegistro = fechaRegistro;
        this.presente = presente;
    }
}

