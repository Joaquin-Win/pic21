package com.pic21.domain;

/**
 * Estados posibles de una tarea (UML v8).
 *
 * PENDIENTE  → tarea asignada, no completada.
 * COMPLETADA → entregada por el usuario.
 * BLOQUEADA  → bloqueada por el sistema.
 */
public enum EstadoTarea {
    PENDIENTE,
    COMPLETADA,
    BLOQUEADA
}
