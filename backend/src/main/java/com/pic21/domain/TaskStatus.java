package com.pic21.domain;

/**
 * Estados posibles de una tarea en PIC21.
 *
 * PENDING     → Tarea asignada, aún no iniciada.
 * IN_PROGRESS → El estudiante está trabajando en ella.
 * DONE        → Completada y entregada.
 * CANCELLED   → Cancelada por PROFESOR/ADMIN.
 */
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    DONE,
    CANCELLED
}
