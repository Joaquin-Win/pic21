package com.pic21.dto.request;

import com.pic21.domain.MeetingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request para cambiar el estado de una reunión (PATCH /api/meetings/{id}/status).
 */
@Getter
@Setter
public class MeetingStatusRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private MeetingStatus status;
}
