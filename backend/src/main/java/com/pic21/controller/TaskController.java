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

/**
 * Controlador de tareas (Task) — PIC21.
 *
 * Endpoints:
 *   POST /api/tasks/meeting/{meetingId}  → Crea y asigna tareas a los estudiantes ausentes
 *                                          Acceso: PROFESOR, AYUDANTE, ADMIN
 *
 *   GET  /api/tasks/my                   → Lista las tareas asignadas al usuario autenticado
 *                                          Acceso: cualquier usuario autenticado
 *
 * La validación de roles se delega al servicio mediante @PreAuthorize.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // -----------------------------------------------------------------------
    // POST /api/tasks/meeting/{meetingId}
    // Crear y asignar tarea a estudiantes ausentes
    // Solo PROFESOR, AYUDANTE, ADMIN
    // -----------------------------------------------------------------------

    /**
     * Crea una tarea y la asigna automáticamente a todos los estudiantes
     * que NO registraron asistencia en la reunión indicada.
     *
     * <p>Reglas del servicio:
     * <ul>
     *   <li>La reunión debe existir.</li>
     *   <li>La reunión no puede estar en NO_INICIADA.</li>
     *   <li>Debe haber al menos un estudiante ausente.</li>
     *   <li>No se asignan tareas duplicadas (si el estudiante ya tiene una, se omite).</li>
     * </ul>
     *
     * @param meetingId   ID de la reunión
     * @param request     DTO con título, descripción y link de la tarea
     * @param userDetails usuario autenticado inyectado por Spring Security
     * @return 201 Created con la lista de tareas creadas
     */
    @PostMapping("/meeting/{meetingId}")
    @PreAuthorize("hasAnyRole('PROFESOR','AYUDANTE','ADMIN')")
    public ResponseEntity<List<TaskResponse>> createForAbsent(
            @PathVariable Long meetingId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TaskResponse> tasks = taskService.createForAbsent(
                meetingId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(tasks);
    }

    // -----------------------------------------------------------------------
    // GET /api/tasks/my
    // Ver mis tareas — cualquier usuario autenticado
    // -----------------------------------------------------------------------

    /**
     * Retorna todas las tareas asignadas al usuario autenticado.
     *
     * <p>Un ESTUDIANTE solo verá sus propias tareas.
     * Un PROFESOR/AYUDANTE/ADMIN también verá las tareas que les asignaron a ellos
     * (aunque normalmente no se les asignan tareas a estos roles).
     *
     * @param userDetails usuario autenticado inyectado por Spring Security
     * @return 200 OK con lista de tareas
     */
    @GetMapping("/my")
    public ResponseEntity<List<TaskResponse>> getMyTasks(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(taskService.findMyTasks(userDetails.getUsername()));
    }
}
