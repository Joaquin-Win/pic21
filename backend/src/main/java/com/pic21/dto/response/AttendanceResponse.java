package com.pic21.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Respuesta con los datos de una asistencia registrada.
 */
@Getter
@Builder
public class AttendanceResponse {

    private Long id;

    private Long meetingId;
    private String meetingTitle;

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    private String legajo;
    private String carrera;
    private String tipoUsuario;

    private LocalDateTime registeredAt;
}

