package com.pic21.dto.response;

import com.pic21.domain.EstadoReunion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MeetingResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private EstadoReunion estado;

    private String accessCode;
    private String recordingLink;
    private String presentacionLink;
    private String newsLink;
    private String activityLink;

    /** Links adicionales */
    private List<String> linksExtra;

    /** Links de noticias adicionales */
    private List<String> newsLinksExtra;

    private String pdfFileName;
    private boolean hasPdfFile;

    private String creadoPorUsername;
    private LocalDateTime createdAt;
}
