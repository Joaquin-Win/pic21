package com.pic21.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Datos del formulario de registro de asistencia.
 * El sistema usa el usuario autenticado (JWT) como identidad,
 * pero captura legajo y materia para el registro de asistencia.
 */
@Getter
@NoArgsConstructor
public class AttendanceRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    private String apellido;

    @Email(message = "El correo institucional debe ser válido")
    @NotBlank(message = "El correo institucional es requerido")
    private String correoInstitucional;

    @NotBlank(message = "El legajo es requerido")
    private String legajo;

    @NotBlank(message = "La carrera es requerida")
    private String carrera;
}
