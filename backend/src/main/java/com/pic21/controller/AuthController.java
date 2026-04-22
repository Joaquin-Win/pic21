package com.pic21.controller;

import com.pic21.dto.request.LoginRequest;
import com.pic21.dto.request.RegisterRequest;
import com.pic21.dto.response.AuthResponse;
import com.pic21.dto.response.UserResponse;
import com.pic21.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.pic21.exception.BusinessException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controlador de autenticación (UML v8).
 *   POST /api/auth/login           → público
 *   POST /api/auth/register        → solo R04_ADMIN
 *   POST /api/auth/register-public → público (R02_ESTUDIANTE o R03_EGRESADO)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String key = request.getUsername();
        AtomicInteger attempts = loginAttempts.computeIfAbsent(key, k -> new AtomicInteger(0));
        if (attempts.get() >= 5) {
            throw new BusinessException("Demasiados intentos de login. Esperá unos minutos antes de reintentar.");
        }
        try {
            AuthResponse response = authService.login(request);
            loginAttempts.remove(key);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            attempts.incrementAndGet();
            throw ex;
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * Registro público: el rol lo determina el cliente
     * (R02_ESTUDIANTE, R03_EGRESADO o R01_PROFESOR según el formulario).
     * Si no se especifica rol, se asigna R02_ESTUDIANTE por defecto.
     */
    @PostMapping("/register-public")
    public ResponseEntity<UserResponse> registerPublic(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }
}
