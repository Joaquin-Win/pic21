package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Request para crear o actualizar una reunión.
 */
@Getter
@Setter
public class MeetingRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String title;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;

    @NotNull(message = "La fecha y hora de la reunión es obligatoria")
    private LocalDateTime scheduledAt;

    /**
     * Código de acceso opcional para la reunión.
     */
    @Size(max = 30, message = "El código de acceso no puede superar los 30 caracteres")
    private String accessCode;
}
