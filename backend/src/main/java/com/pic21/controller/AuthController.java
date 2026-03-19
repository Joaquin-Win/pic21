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

/**
 * Controlador de autenticación.
 * <ul>
 *   <li>POST /api/auth/login      → público</li>
 *   <li>POST /api/auth/register   → solo ADMIN</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login de usuario. Devuelve un JWT si las credenciales son correctas.
     *
     * @param request { username, password }
     * @return { token, type, id, username, email, roles }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Registro de un nuevo usuario. Requiere rol ADMIN.
     *
     * @param request { username, email, password, firstName, lastName, role }
     * @return datos del usuario creado (201 Created)
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * Registro público — cualquier persona puede crear su cuenta.
     * El rol se fuerza a ESTUDIANTE (se ignora lo que mande el request).
     */
    @PostMapping("/register-public")
    public ResponseEntity<UserResponse> registerPublic(@Valid @RequestBody RegisterRequest request) {
        request.setRole(null); // fuerza ESTUDIANTE por defecto
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }
}
