package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la creación de una tarea.
 *
 * El meeting_id viene en el path, no en el body.
 * El assignedTo se asocia automáticamente (todos los ausentes) o se especifica aquí.
 */
@Getter
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "El título de la tarea es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String title;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;

    /**
     * URL opcional (link a un documento, formulario, Google Classroom, etc.)
     */
    @Size(max = 500, message = "El link no puede superar los 500 caracteres")
    private String link;
}
