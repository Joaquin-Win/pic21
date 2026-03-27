package com.pic21.dto.response;

import com.pic21.domain.MeetingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Respuesta pública de una reunión (nunca expone la entidad JPA directamente).
 */
@Getter
@Builder
public class MeetingResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime scheduledAt;
    private MeetingStatus status;

    /**
     * Sólo se expone el accessCode al usuario si la reunión está ACTIVA.
     * La lógica de ocultamiento está en el MeetingService.
     */
    private String accessCode;

    private String recordingLink;
    private String newsLink;
    private String activityLink;

    private String pdfFileName;
    private boolean hasPdfFile;

    private String createdBy;     // username del creador
    private LocalDateTime createdAt;
}
