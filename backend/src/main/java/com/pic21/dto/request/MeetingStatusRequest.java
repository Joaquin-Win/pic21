package com.pic21.dto.request;

import com.pic21.domain.EstadoReunion;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request para cambiar el estado de una reunión.
 */
@Getter
@Setter
public class MeetingStatusRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoReunion estado;
}
