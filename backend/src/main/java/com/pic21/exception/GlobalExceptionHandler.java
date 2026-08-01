/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.GlobalExceptionHandler
 *  com.pic21.exception.GlobalExceptionHandler$ErrorResponse
 *  com.pic21.exception.ResourceNotFoundException
 *  jakarta.servlet.http.HttpServletRequest
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.dao.DataIntegrityViolationException
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.converter.HttpMessageNotReadableException
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.security.authentication.BadCredentialsException
 *  org.springframework.transaction.TransactionSystemException
 *  org.springframework.validation.FieldError
 *  org.springframework.web.bind.MethodArgumentNotValidException
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.RestControllerAdvice
 */
package com.pic21.exception;

import com.pic21.exception.BusinessException;
import com.pic21.exception.GlobalExceptionHandler;
import com.pic21.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value={MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HashMap fieldErrors = new HashMap();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError)error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("fields", fieldErrors);
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(value={HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("[GlobalExceptionHandler] JSON malformado en {}: {}", (Object)request.getRequestURI(), (Object)ex.getMessage());
        return this.build(HttpStatus.BAD_REQUEST, "Bad Request", "El cuerpo de la solicitud es inv\u00e1lido o est\u00e1 mal formado. Verific\u00e1 el JSON enviado.", request);
    }

    @ExceptionHandler(value={BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return this.build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Credenciales inv\u00e1lidas", request);
    }

    @ExceptionHandler(value={AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return this.build(HttpStatus.FORBIDDEN, "Forbidden", "No ten\u00e9s permisos para realizar esta acci\u00f3n", request);
    }

    @ExceptionHandler(value={ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return this.build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(value={BusinessException.class})
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return this.build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(value={DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String rootMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("[Handler] DataIntegrityViolationException en {}: {}", (Object)request.getRequestURI(), (Object)rootMsg);
        String message = rootMsg != null && (rootMsg.contains("unique") || rootMsg.contains("duplicate") || rootMsg.contains("Unique") || rootMsg.contains("llave duplicada")) ? "Registro duplicado. Ya existe un dato igual en el sistema." : (rootMsg != null && (rootMsg.contains("foreign key") || rootMsg.contains("violates foreign key") || rootMsg.contains("is still referenced") || rootMsg.contains("referential integrity")) ? "No se puede realizar esta acci\u00f3n porque el registro tiene datos asociados." : "No se pudo completar la operaci\u00f3n por un conflicto en los datos.");
        return this.build(HttpStatus.CONFLICT, "Conflict", message, request);
    }

    @ExceptionHandler(value={Exception.class})
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("[GlobalExceptionHandler] tipo={} mensaje='{}' path={}", new Object[]{ex.getClass().getName(), ex.getMessage(), request.getRequestURI(), ex});
        if (ex instanceof TransactionSystemException) {
            TransactionSystemException tse = (TransactionSystemException)ex;
            Throwable appEx = tse.getApplicationException();
            if (appEx instanceof BusinessException) {
                BusinessException be = (BusinessException)appEx;
                return this.build(HttpStatus.CONFLICT, "Conflict", be.getMessage(), request);
            }
            if (appEx instanceof ResourceNotFoundException) {
                ResourceNotFoundException rne = (ResourceNotFoundException)appEx;
                return this.build(HttpStatus.NOT_FOUND, "Not Found", rne.getMessage(), request);
            }
        }
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof BusinessException) {
                BusinessException be = (BusinessException)cause;
                log.warn("[Handler] BusinessException encontrada en cadena de causas");
                return this.build(HttpStatus.CONFLICT, "Conflict", be.getMessage(), request);
            }
            if (cause instanceof ResourceNotFoundException) {
                ResourceNotFoundException rne = (ResourceNotFoundException)cause;
                log.warn("[Handler] ResourceNotFoundException encontrada en cadena de causas");
                return this.build(HttpStatus.NOT_FOUND, "Not Found", rne.getMessage(), request);
            }
            if (cause instanceof AccessDeniedException) {
                AccessDeniedException ade = (AccessDeniedException)cause;
                return this.build(HttpStatus.FORBIDDEN, "Forbidden", "No ten\u00e9s permisos para realizar esta acci\u00f3n", request);
            }
            if (!(cause instanceof TransactionSystemException)) continue;
            TransactionSystemException tse2 = (TransactionSystemException)cause;
            Throwable appEx = tse2.getApplicationException();
            if (appEx instanceof BusinessException) {
                BusinessException be = (BusinessException)appEx;
                return this.build(HttpStatus.CONFLICT, "Conflict", be.getMessage(), request);
            }
            if (!(appEx instanceof ResourceNotFoundException)) continue;
            ResourceNotFoundException rne = (ResourceNotFoundException)appEx;
            return this.build(HttpStatus.NOT_FOUND, "Not Found", rne.getMessage(), request);
        }
        return this.build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ocurri\u00f3 un error inesperado. Por favor, intent\u00e1 de nuevo.", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, HttpServletRequest request) {
        return ResponseEntity.status((HttpStatusCode)status).body((Object)new ErrorResponse(LocalDateTime.now().toString(), status.value(), error, message, request.getRequestURI()));
    }
}

