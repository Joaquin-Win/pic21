package com.pic21.dto.response;

import com.pic21.domain.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta con los datos de una tarea general.
 * Incluye estadísticas de asignaciones y opcionalmente la lista de asignados.
 */
@Getter
@Builder
public class TaskResponse {

    private Long id;

    private Long meetingId;
    private String meetingTitle;

    private String title;
    private String description;
    private String link;
    private String questionsJson;

    private Long createdById;
    private String createdByUsername;

    private LocalDateTime createdAt;

    /** Total de usuarios asignados a esta tarea. */
    private long assignmentCount;

    /** Cuántos tienen estado PENDING. */
    private long pendingCount;

    /** Lista de asignaciones (incluida en vistas admin/professor). */
    private List<TaskAssignmentResponse> assignments;
}
