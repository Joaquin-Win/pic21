package com.pic21.domain;

/**
 * Estados posibles de una reunión en PIC21.
 *
 * Transiciones válidas:
 *   NO_INICIADA → ACTIVA         (abrir la reunión)
 *   ACTIVA      → BLOQUEADA      (cerrar registro de asistencia)
 *
 * Regla: No se pueden hacer cambios (editar/cambiar estado) si está BLOQUEADA.
 */
public enum MeetingStatus {
    NO_INICIADA,
    ACTIVA,
    BLOQUEADA
}
