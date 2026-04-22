package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MeetingRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "La fecha y hora es obligatoria")
    private LocalDateTime scheduledAt;

    private String accessCode;
    private String recordingLink;
    private String presentacionLink;
    private String newsLink;
    private String activityLink;

    /** Lista de links extra adicionales */
    private List<String> linksExtra;

    /** Lista de links de noticias adicionales (además de newsLink principal). */
    private List<String> newsLinksExtra;
}
