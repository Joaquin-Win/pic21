package com.pic21.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un archivo PDF de una reunión.
 */
@Data
@Builder
public class MeetingFileResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long meetingId;
    private String meetingTitle;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
    /** Tamaño en bytes del archivo */
    private Long fileSize;
}
