/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.AuthController
 *  com.pic21.dto.request.LoginRequest
 *  com.pic21.dto.request.RegisterRequest
 *  com.pic21.dto.response.AuthResponse
 *  com.pic21.dto.response.UserResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.service.AuthService
 *  jakarta.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.pic21.controller;

import com.pic21.dto.request.LoginRequest;
import com.pic21.dto.request.RegisterRequest;
import com.pic21.dto.response.AuthResponse;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth"})
public class AuthController {
    private final AuthService authService;
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap();

    @PostMapping(value={"/login"})
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String key = request.getUsername();
        AtomicInteger attempts = this.loginAttempts.computeIfAbsent(key, k -> new AtomicInteger(0));
        if (attempts.get() >= 5) {
            throw new BusinessException("Demasiados intentos de login. Esper\u00e1 unos minutos antes de reintentar.");
        }
        try {
            AuthResponse response = this.authService.login(request);
            this.loginAttempts.remove(key);
            return ResponseEntity.ok(response);
        }
        catch (Exception ex) {
            attempts.incrementAndGet();
            throw ex;
        }
    }

    @PostMapping(value={"/register"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(this.authService.register(request));
    }

    @PostMapping(value={"/register-public"})
    public ResponseEntity<UserResponse> registerPublic(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(this.authService.register(request));
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}

