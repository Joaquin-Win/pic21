package com.pic21.domain;

/**
 * Estados posibles de una tarea en PIC21.
 *
 * PENDING    → Tarea asignada, pendiente de entrega.
 * COMPLETED  → Completada y entregada por el estudiante.
 * CORRECTED  → Revisada y corregida por ADMIN/PROFESOR.
 */
public enum TaskStatus {
    PENDING,
    COMPLETED,
    CORRECTED,
    APPROVED
}
