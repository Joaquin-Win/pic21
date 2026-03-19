package com.pic21.controller;

import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskResponse;
import com.pic21.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de Tareas — PIC21
 *
 * POST   /api/tasks/meeting/{id}           → Crear y asignar tareas a ausentes (ADMIN, PROFESOR)
 * GET    /api/tasks/my                     → Mis tareas asignadas (cualquier usuario)
 * GET    /api/tasks                        → Todas las tareas según rol (ADMIN: todas, PROFESOR: propias)
 * GET    /api/tasks/meeting/{id}/pending   → Pendientes por reunión (ADMIN, PROFESOR)
 * PUT    /api/tasks/{id}                   → Editar tarea (ADMIN)
 * DELETE /api/tasks/{id}                   → Eliminar tarea (ADMIN)
 * PATCH  /api/tasks/{id}/status            → Cambiar estado (ADMIN)
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ── Crear tareas para ausentes ─────────────────────────
    @PostMapping("/meeting/{meetingId}")
    @PreAuthorize("hasAnyRole('PROFESOR','ADMIN')")
    public ResponseEntity<List<TaskResponse>> createForAbsent(
            @PathVariable Long meetingId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TaskResponse> tasks = taskService.createForAbsent(
                meetingId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(tasks);
    }

    // ── Mis tareas (usuario autenticado) ───────────────────
    @GetMapping("/my")
    public ResponseEntity<List<TaskResponse>> getMyTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findMyTasks(userDetails.getUsername()));
    }

    // ── Todas las tareas según rol ─────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<TaskResponse>> getAllByRole(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findAllByRole(userDetails.getUsername()));
    }

    // ── Pendientes por reunión ─────────────────────────────
    @GetMapping("/meeting/{meetingId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<TaskResponse>> getPendingByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(taskService.findPendingByMeeting(meetingId));
    }

    // ── Editar tarea (ADMIN) ───────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    // ── Eliminar tarea (ADMIN) ─────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // ── Cambiar estado de tarea (ADMIN) ───────────────────
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(taskService.changeStatus(id, status));
    }
}
