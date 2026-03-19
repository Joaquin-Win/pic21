package com.pic21.dto.response;

import com.pic21.domain.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una asignación individual de tarea.
 */
@Getter
@Builder
public class TaskAssignmentResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;

    private TaskStatus status;
    private LocalDateTime createdAt;
}
