package com.pic21.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Respuesta con los datos de una asistencia registrada (UML v8).
 */
@Getter
@Builder
public class AttendanceResponse {

    private Long id;

    private Long reunionId;
    private String reunionTitulo;

    private Long usuarioId;
    private String username;
    private String nombre;
    private String apellido;
    private String email;

    private boolean presente;
    private LocalDateTime fechaRegistro;
}
