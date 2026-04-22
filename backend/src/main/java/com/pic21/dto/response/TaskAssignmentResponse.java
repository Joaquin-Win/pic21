package com.pic21.dto.response;

import com.pic21.domain.EstadoTarea;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para una asignación individual de tarea (UML v8).
 */
@Getter
@Builder
public class TaskAssignmentResponse {

    private Long id;
    private Long tareaId;

    /** Alias tituloTarea para compatibilidad con el frontend */
    private String tituloTarea;
    private String descripcionTarea;
    private String linkTarea;
    private List<String> linksTarea;

    private Long reunionId;
    private String reunionTitulo;

    private Long usuarioId;
    private String username;
    private String nombre;
    private String apellido;

    private EstadoTarea estado;
    private Integer score;

    /** intentos — alias de attempts para el frontend */
    private int intentos;

    private String questionsJson;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaCompletado;
}
