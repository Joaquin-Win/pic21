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
 * Controlador de Tareas (UML v8).
 */
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/tasks/meeting/{meetingId}")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskAssignmentResponse>> createForAbsent(
            @PathVariable Long meetingId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createForAbsent(meetingId, request, userDetails.getUsername()));
    }

    @GetMapping("/api/tasks/my")
    public ResponseEntity<List<TaskAssignmentResponse>> getMyAssignments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findMyAssignments(userDetails.getUsername()));
    }

    @GetMapping("/api/tasks")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskResponse>> getAllByRole(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.findAllByRole(userDetails.getUsername()));
    }

    @GetMapping("/api/tasks/{id}/assignments")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskAssignmentResponse>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getAssignments(id));
    }

    @GetMapping("/api/tasks/meeting/{meetingId}/pending")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskResponse>> getPendingByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(taskService.findPendingByMeeting(meetingId));
    }

    @PutMapping("/api/tasks/{id}")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/api/tasks/{id}")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/tasks/{id}/block")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<TaskResponse> blockTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.blockTask(id));
    }

    @PatchMapping("/api/task-assignments/{id}/status")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<TaskAssignmentResponse> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(taskService.changeAssignmentStatus(id, body.get("status")));
    }

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
