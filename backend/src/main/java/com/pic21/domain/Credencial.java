/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Credencial
 *  com.pic21.domain.Credencial$CredencialBuilder
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.UpdateTimestamp
 */
package com.pic21.domain;
import lombok.Builder;

import com.pic21.domain.Credencial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.UpdateTimestamp;

/*
 * Exception performing whole class analysis ignored.
 */
@Entity
@Table(name="credenciales")
public class Credencial {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=150)
    private String email;
    @Column(name="password_hash", nullable=false)
    private String passwordHash;
    @UpdateTimestamp
    @Column(name="ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
    @Column(name="intentos_fallidos", nullable=false)
    private int intentosFallidos;
    @Column(name="bloqueada_hasta")
    private LocalDateTime bloqueadaHasta;

    private static int $default$intentosFallidos() {
        return 0;
    }
    public Long getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public LocalDateTime getUltimaActualizacion() {
        return this.ultimaActualizacion;
    }

    public int getIntentosFallidos() {
        return this.intentosFallidos;
    }

    public LocalDateTime getBloqueadaHasta() {
        return this.bloqueadaHasta;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public void setBloqueadaHasta(LocalDateTime bloqueadaHasta) {
        this.bloqueadaHasta = bloqueadaHasta;
    }

    public Credencial() {
        this.intentosFallidos = Credencial.$default$intentosFallidos();
    }

    @Builder
    public Credencial(Long id, String email, String passwordHash, LocalDateTime ultimaActualizacion, int intentosFallidos, LocalDateTime bloqueadaHasta) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.ultimaActualizacion = ultimaActualizacion;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadaHasta = bloqueadaHasta;
    }
}

