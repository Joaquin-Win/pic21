package com.pic21.dto.response;

import com.pic21.domain.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Respuesta con los datos de una tarea.
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

    private Long assignedToId;
    private String assignedToUsername;
    private String assignedToFirstName;
    private String assignedToLastName;

    private Long createdById;
    private String createdByUsername;

    private TaskStatus status;

    private LocalDateTime createdAt;
}
