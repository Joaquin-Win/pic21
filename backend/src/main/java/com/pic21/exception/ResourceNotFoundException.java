package com.pic21.exception;

/**
 * Excepción para recursos no encontrados (404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " con id " + id + " no encontrado");
    }
}
