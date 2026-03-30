package com.pic21.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones.
 *
 * Estrategia: camina la cadena de causas (cause chain) para encontrar
 * la excepción de negocio original, incluso si está envuelta por AOP/CGLIB.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // DTO de respuesta de error
    // -------------------------------------------------------------------------
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}

    // -------------------------------------------------------------------------
    // 400 — Validación de campos (@Valid)
    // -------------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("fields", fieldErrors);
        body.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    // -------------------------------------------------------------------------
    // 400 — JSON malformado o cuerpo ilegible
    // -------------------------------------------------------------------------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] JSON malformado en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request",
                "El cuerpo de la solicitud es inválido o está mal formado. Verificá el JSON enviado.",
                request);
    }

    // -------------------------------------------------------------------------
    // 401 — Credenciales inválidas
    // -------------------------------------------------------------------------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Credenciales inválidas", request);
    }

    // -------------------------------------------------------------------------
    // 403 — Sin permisos
    // -------------------------------------------------------------------------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return build(HttpStatus.FORBIDDEN, "Forbidden",
                "No tenés permisos para realizar esta acción", request);
    }

    // -------------------------------------------------------------------------
    // 404 — Recurso no encontrado
    // -------------------------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 409 — Regla de negocio violada
    // -------------------------------------------------------------------------
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 409 — Conflicto de restricción de BD (ej: asistencia duplicada, FK)
    // -------------------------------------------------------------------------
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String rootMsg = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("[Handler] DataIntegrityViolationException en {}: {}", request.getRequestURI(), rootMsg);

        // Determinar si es duplicate key o FK constraint
        String message;
        if (rootMsg != null && (rootMsg.contains("unique") || rootMsg.contains("duplicate")
                || rootMsg.contains("Unique") || rootMsg.contains("llave duplicada"))) {
            message = "Registro duplicado. Ya existe un dato igual en el sistema.";
        } else if (rootMsg != null && (rootMsg.contains("foreign key") || rootMsg.contains("violates foreign key")
                || rootMsg.contains("is still referenced") || rootMsg.contains("referential integrity"))) {
            message = "No se puede realizar esta acción porque el registro tiene datos asociados.";
        } else {
            message = "No se pudo completar la operación por un conflicto en los datos.";
        }

        return build(HttpStatus.CONFLICT, "Conflict", message, request);
    }

    // -------------------------------------------------------------------------
    // Catch-all — camina la cadena de causas para encontrar la excepción real
    // -------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("[GlobalExceptionHandler] tipo={} mensaje='{}' path={}",
                ex.getClass().getName(), ex.getMessage(), request.getRequestURI(), ex);

        // Caso especial: Spring @Transactional puede envolver excepciones de negocio
        // en TransactionSystemException almacenándolas via setApplicationException()
        // (NO como getCause()), por eso chequeamos getApplicationException() primero.
        if (ex instanceof org.springframework.transaction.TransactionSystemException tse) {
            Throwable appEx = tse.getApplicationException();
            if (appEx instanceof BusinessException be) {
                return build(HttpStatus.CONFLICT, "Conflict", be.getMessage(), request);
            }
            if (appEx instanceof ResourceNotFoundException rne) {
                return build(HttpStatus.NOT_FOUND, "Not Found", rne.getMessage(), request);
            }
        }

        // Caminar la cadena de causas: Spring AOP/CGLIB puede envolver excepciones
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof BusinessException be) {
                log.warn("[Handler] BusinessException encontrada en cadena de causas");
                return build(HttpStatus.CONFLICT, "Conflict", be.getMessage(), request);
            }
            if (cause instanceof ResourceNotFoundException rne) {
                log.warn("[Handler] ResourceNotFoundException encontrada en cadena de causas");
                return build(HttpStatus.NOT_FOUND, "Not Found", rne.getMessage(), request);
            }
            if (cause instanceof AccessDeniedException ade) {
                return build(HttpStatus.FORBIDDEN, "Forbidden",
                        "No tenés permisos para realizar esta acción", request);
            }
            if (cause instanceof org.springframework.transaction.TransactionSystemException tse2) {
                Throwable appEx = tse2.getApplicationException();
                if (appEx instanceof BusinessException be) {
                    return build(HttpStatus.CONFLICT, "Conflict", be.getMessage(), request);
                }
                if (appEx instanceof ResourceNotFoundException rne) {
                    return build(HttpStatus.NOT_FOUND, "Not Found", rne.getMessage(), request);
                }
            }
            cause = cause.getCause();
        }

        // Error verdaderamente inesperado — nunca exponer detalles internos
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ocurrió un error inesperado. Por favor, intentá de nuevo.", request);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error,
                                                 String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        LocalDateTime.now().toString(),
                        status.value(),
                        error,
                        message,
                        request.getRequestURI()
                )
        );
    }
}
