/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.TaskController
 *  com.pic21.dto.request.TaskRequest
 *  com.pic21.dto.response.TaskAssignmentResponse
 *  com.pic21.dto.response.TaskResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.service.TaskService
 *  jakarta.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PatchMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RestController
 */
package com.pic21.controller;

import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskAssignmentResponse;
import com.pic21.dto.response.TaskResponse;
import com.pic21.exception.BusinessException;
import com.pic21.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {
    private final TaskService taskService;

    @PostMapping(value={"/api/tasks/meeting/{meetingId}"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskAssignmentResponse>> createForAbsent(@PathVariable Long meetingId, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(this.taskService.createForAbsent(meetingId, request, userDetails.getUsername()));
    }

    @GetMapping(value={"/api/tasks/my"})
    public ResponseEntity<List<TaskAssignmentResponse>> getMyAssignments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(this.taskService.findMyAssignments(userDetails.getUsername()));
    }

    @GetMapping(value={"/api/tasks"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskResponse>> getAllByRole(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(this.taskService.findAllByRole(userDetails.getUsername()));
    }

    @GetMapping(value={"/api/tasks/{id}/assignments"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskAssignmentResponse>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(this.taskService.getAssignments(id));
    }

    @PostMapping(value={"/api/tasks/{id}/add-users"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<List<TaskAssignmentResponse>> addUsersToTask(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> userIds = body.get("userIds");
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("El campo 'userIds' es requerido.");
        }
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(this.taskService.addUsersToTask(id, userIds));
    }

    @GetMapping(value={"/api/tasks/meeting/{meetingId}/pending"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<TaskResponse>> getPendingByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(this.taskService.findPendingByMeeting(meetingId));
    }

    @PutMapping(value={"/api/tasks/{id}"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(this.taskService.updateTask(id, request));
    }

    @DeleteMapping(value={"/api/tasks/{id}"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value={"/api/tasks/{id}/block"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<TaskResponse> blockTask(@PathVariable Long id) {
        return ResponseEntity.ok(this.taskService.blockTask(id));
    }

    @PatchMapping(value={"/api/tasks/{id}/unblock"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<TaskResponse> unblockTask(@PathVariable Long id) {
        return ResponseEntity.ok(this.taskService.unblockTask(id));
    }

    @PatchMapping(value={"/api/task-assignments/{id}/status"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<TaskAssignmentResponse> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(this.taskService.changeAssignmentStatus(id, body.get("status")));
    }

    @PostMapping(value={"/api/task-assignments/{id}/submit"})
    public ResponseEntity<TaskAssignmentResponse> submitQuiz(@PathVariable Long id, @RequestBody Map<String, Object> body, @AuthenticationPrincipal UserDetails userDetails) {
        List answers = ((List)body.get("answers")).stream().map(a -> a instanceof Number ? ((Number)a).intValue() : Integer.parseInt(a.toString())).collect(Collectors.toList());
        return ResponseEntity.ok(this.taskService.submitQuiz(id, answers, userDetails.getUsername()));
    }

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
}

