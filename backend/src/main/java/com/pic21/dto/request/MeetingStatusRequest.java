/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.dto.request.MeetingStatusRequest
 *  jakarta.validation.constraints.NotNull
 */
package com.pic21.dto.request;

import com.pic21.domain.EstadoReunion;
import jakarta.validation.constraints.NotNull;

public class MeetingStatusRequest {
    @NotNull(message="El nuevo estado es obligatorio")
    private @NotNull(message="El nuevo estado es obligatorio") EstadoReunion estado;

    public EstadoReunion getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoReunion estado) {
        this.estado = estado;
    }
}

