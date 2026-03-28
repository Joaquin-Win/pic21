package com.pic21.controller;

import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskAssignmentResponse;
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
 * POST   /api/tasks/meeting/{id}            → Crear tarea general + asignaciones a ausentes (ADMIN, PROFESOR)
 * GET    /api/tasks/my                      → Mis asignaciones (ESTUDIANTE, AYUDANTE)
 * GET    /api/tasks                         → Todas las tareas generales según rol (ADMIN, PROFESOR)
 * GET    /api/tasks/{id}/assignments        → Asignaciones de una tarea (ADMIN, PROFESOR)
 * GET    /api/tasks/meeting/{id}/pending    → Tareas con pendientes en una reunión (ADMIN, PROFESOR)
 * PUT    /api/tasks/{id}                    → Editar tarea general — afecta a todos (ADMIN)
 * DELETE /api/tasks/{id}                    → Eliminar tarea + todas sus asignaciones (ADMIN)
 * PATCH  /api/task-assignments/{id}/status  → Cambiar estado de una asignación individual (ADMIN)
 */
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ── Crear tarea para ausentes ──────────────────────────
    @PostMapping("/api/tasks/meeting/{meetingId}")
    @PreAuthorize("hasAnyRole('PROFESOR','ADMIN')")
    public ResponseEntity<List<TaskAssignmentResponse>> createForAbsent(
            @PathVariable Long meetingId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createForAbsent(meetingId, request, userDetails.getUsername()));
    }

    // ── Mis asignaciones (ESTUDIANTE / AYUDANTE) ──────────
    @GetMapping("/api/tasks/my")
    public ResponseEntity<List<TaskAssignmentResponse>> getMyAssignments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findMyAssignments(userDetails.getUsername()));
    }

    // ── Todas las tareas generales según rol ──────────────
    @GetMapping("/api/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<TaskResponse>> getAllByRole(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findAllByRole(userDetails.getUsername()));
    }

    // ── Asignaciones de una tarea ─────────────────────────
    @GetMapping("/api/tasks/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<TaskAssignmentResponse>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getAssignments(id));
    }

    // ── Tareas con pendientes en una reunión ──────────────
    @GetMapping("/api/tasks/meeting/{meetingId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<TaskResponse>> getPendingByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(taskService.findPendingByMeeting(meetingId));
    }

    // ── Editar tarea general (ADMIN) ──────────────────────
    @PutMapping("/api/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    // ── Eliminar tarea + assignments (ADMIN) ──────────────
    @DeleteMapping("/api/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // ── Cambiar estado de una asignación (ADMIN) ──────────
    @PatchMapping("/api/task-assignments/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskAssignmentResponse> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(taskService.changeAssignmentStatus(id, body.get("status")));
    }

    // ── Submit quiz (ESTUDIANTE / AYUDANTE) ──────────────
    @PostMapping("/api/task-assignments/{id}/submit")
    public ResponseEntity<TaskAssignmentResponse> submitQuiz(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        @SuppressWarnings("unchecked")
        List<Integer> answers = ((List<?>) body.get("answers")).stream()
                .map(a -> a instanceof Number ? ((Number) a).intValue() : Integer.parseInt(a.toString()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(taskService.submitQuiz(id, answers, userDetails.getUsername()));
    }
}
