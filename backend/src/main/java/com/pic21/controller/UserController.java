package com.pic21.controller;

import com.pic21.dto.request.UpdateUserRequest;
import com.pic21.dto.response.UserResponse;
import com.pic21.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de gestión de usuarios — acceso exclusivo para ADMIN.
 *
 * Endpoints:
 *   GET    /api/users              → listar todos
 *   GET    /api/users/{id}         → ver uno
 *   PUT    /api/users/{id}/roles   → actualizar roles
 *   PATCH  /api/users/{id}/toggle  → habilitar / deshabilitar
 *   DELETE /api/users/{id}         → eliminar
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Actualiza el perfil (nombre, apellido, email, username) de un usuario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok(userService.updateProfile(id, request, me.getUsername()));
    }

    /**
     * Actualiza los roles de un usuario.
     * Body: { "roles": ["ADMIN", "PROFESOR"] }
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateRoles(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> body,
            @AuthenticationPrincipal UserDetails me) {
        List<String> roles = body.get("roles");
        if (roles == null || roles.isEmpty()) {
            throw new com.pic21.exception.BusinessException("El campo 'roles' es requerido y no puede estar vacío.");
        }
        return ResponseEntity.ok(userService.updateRoles(id, roles, me.getUsername()));
    }

    /**
     * Habilita o deshabilita un usuario (toggle).
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<UserResponse> toggleEnabled(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok(userService.toggleEnabled(id, me.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails me) {
        userService.delete(id, me.getUsername());
        return ResponseEntity.noContent().build();
    }
}
