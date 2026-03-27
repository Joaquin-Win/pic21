package com.pic21.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Datos del formulario de registro de asistencia.
 * El sistema usa el usuario autenticado (JWT) como identidad,
 * pero captura legajo, carrera y tipo de usuario para el registro.
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

    /** Carrera — obligatorio solo si tipoUsuario es "Alumno" */
    private String carrera;

    /** Tipo de usuario: "Alumno" o "Egresado" */
    @NotBlank(message = "El tipo de usuario es requerido")
    private String tipoUsuario;
}
