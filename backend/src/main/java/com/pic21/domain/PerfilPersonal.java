/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.PerfilPersonal
 *  com.pic21.domain.PerfilPersonal$PerfilPersonalBuilder
 *  jakarta.persistence.Column
 *  jakarta.persistence.Embeddable
 */
package com.pic21.domain;

import com.pic21.domain.PerfilPersonal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PerfilPersonal {
    @Column(name="dni", length=8)
    private String dni;
    @Column(name="correo", length=150)
    private String correo;

    public static PerfilPersonalBuilder builder() {
        return new PerfilPersonalBuilder();
    }

    public String getDni() {
        return this.dni;
    }

    public String getCorreo() {
        return this.correo;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public PerfilPersonal() {
    }

    public PerfilPersonal(String dni, String correo) {
        this.dni = dni;
        this.correo = correo;
    }
}

