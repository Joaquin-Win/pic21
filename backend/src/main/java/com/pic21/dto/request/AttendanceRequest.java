package com.pic21.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Datos del formulario de registro de asistencia.
 * El sistema usa el usuario autenticado (JWT) como identidad.
 * El campo presente indica si se registra la asistencia como presente.
 */
@Getter
@NoArgsConstructor
public class AttendanceRequest {

    /** True = presente, false = ausente. Por defecto presente. */
    private boolean presente = true;
}
