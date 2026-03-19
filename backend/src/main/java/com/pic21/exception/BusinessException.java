package com.pic21.exception;

/**
 * Excepción para violaciones de reglas de negocio (409 Conflict).
 * Ejemplos: asistencia duplicada, reunión no ACTIVA, etc.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
