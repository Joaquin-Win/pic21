/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.PerfilEstudiantil
 *  com.pic21.domain.PerfilEstudiantil$PerfilEstudiantilBuilder
 *  jakarta.persistence.Column
 *  jakarta.persistence.Embeddable
 */
package com.pic21.domain;
import lombok.Builder;

import com.pic21.domain.PerfilEstudiantil;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PerfilEstudiantil {
    @Column(name="correo_institucional", length=150)
    private String correoInstitucional;
    @Column(name="legajo", length=20)
    private String legajo;
    @Column(name="carrera", length=150)
    private String carrera;
    public String getCorreoInstitucional() {
        return this.correoInstitucional;
    }

    public String getLegajo() {
        return this.legajo;
    }

    public String getCarrera() {
        return this.carrera;
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

    public PerfilEstudiantil() {
    }

    @Builder
    public PerfilEstudiantil(String correoInstitucional, String legajo, String carrera) {
        this.correoInstitucional = correoInstitucional;
        this.legajo = legajo;
        this.carrera = carrera;
    }
}

