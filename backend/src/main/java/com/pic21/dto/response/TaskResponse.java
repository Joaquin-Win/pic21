package com.pic21.dto.response;

import com.pic21.domain.EstadoTarea;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de una tarea general (UML v8).
 */
@Getter
@Builder
public class TaskResponse {

    private Long id;

    private Long reunionId;
    private String reunionTitulo;

    private String titulo;
    private String descripcion;
    private String link;
    private List<String> links;
    private String questionsJson;
    private EstadoTarea estado;

    private Long creadoPorId;
    private String creadoPorUsername;

    private LocalDateTime createdAt;

    private long totalAsignaciones;
    private long pendientes;

    private List<TaskAssignmentResponse> asignaciones;
}
