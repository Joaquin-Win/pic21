package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.domain.Role.RoleName;
import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskAssignmentResponse;
import com.pic21.dto.response.TaskResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AttendanceRepository;
import com.pic21.repository.MeetingRepository;
import com.pic21.repository.TaskAssignmentRepository;
import com.pic21.repository.TaskRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de tareas — PIC21.
 *
 * Modelo:
 *   Task          → tarea general (title, description, meeting, createdBy)
 *   TaskAssignment → asignación individual por usuario (task + user + status)
 *
 * Roles:
 *   ADMIN    → ve todas las tareas + edita/elimina/cambia estado de asignaciones
 *   PROFESOR → crea tareas y ve las propias
 *   AYUDANTE → ve sus asignaciones (como ESTUDIANTE); NO puede crear
 *   ESTUDIANTE → ve sus asignaciones
 *
 * Al crear una tarea para una reunión:
 *   → Se crea UN solo Task
 *   → Se crean N TaskAssignment (uno por ESTUDIANTE y AYUDANTE que no asistió)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository           taskRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final MeetingRepository        meetingRepository;
    private final UserRepository           userRepository;
    private final AttendanceRepository     attendanceRepository;

    private static final Set<RoleName> ASSIGNABLE_ROLES = new HashSet<>(Arrays.asList(
            RoleName.ESTUDIANTE, RoleName.AYUDANTE
    ));

    // ── Crear tarea general + asignaciones ────────────────
    @Transactional
    public List<TaskAssignmentResponse> createForAbsent(Long meetingId, TaskRequest request, String creatorUsername) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));

        if (meeting.getStatus() == MeetingStatus.NO_INICIADA) {
            throw new BusinessException("No se pueden crear tareas para una reunión NO_INICIADA.");
        }

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + creatorUsername));

        // IDs de quienes SÍ asistieron
        List<Long> presentIds = attendanceRepository.findByMeetingWithDetails(meeting)
                .stream().map(a -> a.getUser().getId()).collect(Collectors.toList());

        // ESTUDIANTES + AYUDANTES ausentes
        List<User> absentees = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> ASSIGNABLE_ROLES.contains(r.getName())))
                .filter(u -> !presentIds.contains(u.getId()))
                .collect(Collectors.toList());

        if (absentees.isEmpty()) {
            throw new BusinessException(
                    "Todos los estudiantes y ayudantes asistieron a '" + meeting.getTitle() + "'. Sin ausentes.");
        }

        // Crear la tarea general
        Task task = Task.builder()
                .meeting(meeting)
                .title(request.getTitle())
                .description(request.getDescription())
                .link(request.getLink())
                .questionsJson(request.getQuestionsJson())
                .createdBy(creator)
                .build();
        task = taskRepository.save(task);

        // Crear asignaciones (sin duplicar si ya existe)
        final Task savedTask = task;
        List<TaskAssignment> toSave = absentees.stream()
                .filter(u -> !assignmentRepository.existsByTaskIdAndAssignedToId(savedTask.getId(), u.getId()))
                .map(u -> TaskAssignment.builder()
                        .task(savedTask)
                        .assignedTo(u)
                        .status(TaskStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        if (toSave.isEmpty()) {
            throw new BusinessException("Ya existen asignaciones para todos los ausentes en esta tarea.");
        }

        List<TaskAssignment> saved = assignmentRepository.saveAll(toSave);
        log.info("Tarea id={} '{}' creada con {} asignaciones en reunión id={} por '{}'",
                savedTask.getId(), request.getTitle(), saved.size(), meetingId, creatorUsername);

        return saved.stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    // ── Mis tareas asignadas (ESTUDIANTE / AYUDANTE) ───────
    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> findMyAssignments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return assignmentRepository.findByAssignedToId(user.getId())
                .stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    // ── Todas las tareas según rol ─────────────────────────
    @Transactional(readOnly = true)
    public List<TaskResponse> findAllByRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);

        List<Task> tasks = isAdmin
                ? taskRepository.findAllWithDetails()
                : taskRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId());

        return tasks.stream().map(t -> mapTask(t, false)).collect(Collectors.toList());
    }

    // ── Asignaciones de una tarea (admin / profesor) ───────
    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> getAssignments(Long taskId) {
        if (!taskRepository.existsById(taskId)) throw new ResourceNotFoundException("Tarea", taskId);
        return assignmentRepository.findByTaskIdWithUser(taskId)
                .stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    // ── Pendientes por reunión ─────────────────────────────
    @Transactional(readOnly = true)
    public List<TaskResponse> findPendingByMeeting(Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) throw new ResourceNotFoundException("Reunión", meetingId);
        List<Task> tasks = taskRepository.findByMeetingId(meetingId);
        return tasks.stream()
                .filter(t -> assignmentRepository.countByTaskIdAndStatus(t.getId(), TaskStatus.PENDING) > 0)
                .map(t -> mapTask(t, false))
                .collect(Collectors.toList());
    }

    // ── Editar tarea general (ADMIN) ───────────────────────
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setLink(request.getLink());
        task.setQuestionsJson(request.getQuestionsJson());
        log.info("Tarea id={} actualizada (impacta a todos los asignados)", id);
        return mapTask(taskRepository.save(task), false);
    }

    // ── Eliminar tarea (ADMIN) — cascade borra assignments ─
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        taskRepository.delete(task); // cascade ALL + orphanRemoval eliminan las assignments
        log.info("Tarea id={} '{}' eliminada junto con sus asignaciones", id, task.getTitle());
    }

    // ── Cambiar estado de una asignación (ADMIN) ───────────
    @Transactional
    public TaskAssignmentResponse changeAssignmentStatus(Long assignmentId, String statusStr) {
        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignmentId));
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inválido: " + statusStr + ". Válidos: PENDING, COMPLETED, CORRECTED");
        }
        assignment.setStatus(newStatus);
        log.info("Asignación id={} → estado {}", assignmentId, newStatus);
        return mapAssignment(assignmentRepository.save(assignment));
    }

    // ── Submit quiz (ESTUDIANTE / AYUDANTE) ─────────────────
    @Transactional
    public TaskAssignmentResponse submitQuiz(Long assignmentId, List<Integer> answers, String username) {
        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignmentId));

        // Verify the assignment belongs to this user
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        if (!assignment.getAssignedTo().getId().equals(user.getId())) {
            throw new BusinessException("Esta asignación no te pertenece.");
        }

        // Already approved — no need to retry
        if (assignment.getStatus() == TaskStatus.APPROVED) {
            throw new BusinessException("Ya aprobaste este quiz. No es necesario rendirlo nuevamente.");
        }

        // Parse questions
        String questionsJson = assignment.getTask().getQuestionsJson();
        if (questionsJson == null || questionsJson.isBlank()) {
            throw new BusinessException("Esta tarea no tiene un quiz configurado.");
        }

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        List<?> questions;
        try {
            questions = mapper.readValue(questionsJson, List.class);
        } catch (Exception e) {
            throw new BusinessException("Error al leer las preguntas del quiz.");
        }

        if (answers == null || answers.size() != questions.size()) {
            throw new BusinessException("Debés responder todas las preguntas (" + questions.size() + ").");
        }

        // Calculate score
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> q = (java.util.Map<String, Object>) questions.get(i);
            Object correctIdx = q.get("correct");
            int expected = correctIdx instanceof Number ? ((Number) correctIdx).intValue() : -1;
            if (answers.get(i) != null && answers.get(i) == expected) {
                correct++;
            }
        }

        int scorePercent = (int) Math.round((correct * 100.0) / questions.size());

        // UPDATE existing assignment via native query to avoid any Hibernate issues
        int currentAttempts = assignment.getAttempts();
        TaskStatus newStatus = scorePercent >= 70 ? TaskStatus.APPROVED : TaskStatus.PENDING;

        assignmentRepository.updateQuizResult(assignmentId, scorePercent, currentAttempts + 1, newStatus.name());

        log.info("Quiz {}: assignment={}, user='{}', score={}%, intento #{}",
                newStatus == TaskStatus.APPROVED ? "APROBADO" : "NO aprobado",
                assignmentId, username, scorePercent, currentAttempts + 1);

        // Reload and return
        TaskAssignment updated = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignmentId));
        return mapAssignment(updated);
    }

    // ── Helpers ────────────────────────────────────────────
    private TaskResponse mapTask(Task t, boolean includeAssignments) {
        long total   = assignmentRepository.countByTaskId(t.getId());
        long pending = assignmentRepository.countByTaskIdAndStatus(t.getId(), TaskStatus.PENDING);
        return TaskResponse.builder()
                .id(t.getId())
                .meetingId(t.getMeeting().getId())
                .meetingTitle(t.getMeeting().getTitle())
                .title(t.getTitle())
                .description(t.getDescription())
                .link(t.getLink())
                .questionsJson(t.getQuestionsJson())
                .createdById(t.getCreatedBy().getId())
                .createdByUsername(t.getCreatedBy().getUsername())
                .createdAt(t.getCreatedAt())
                .assignmentCount(total)
                .pendingCount(pending)
                .build();
    }

    private TaskAssignmentResponse mapAssignment(TaskAssignment a) {
        // Strip correct answers when returning to student (so they can't cheat)
        String safeQuestions = null;
        String raw = a.getTask().getQuestionsJson();
        if (raw != null && !raw.isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                List<java.util.Map<String, Object>> qs = mapper.readValue(raw,
                        mapper.getTypeFactory().constructCollectionType(List.class, java.util.Map.class));
                qs.forEach(q -> q.remove("correct"));
                safeQuestions = mapper.writeValueAsString(qs);
            } catch (Exception e) {
                safeQuestions = raw; // fallback
            }
        }

        return TaskAssignmentResponse.builder()
                .id(a.getId())
                .taskId(a.getTask().getId())
                .taskTitle(a.getTask().getTitle())
                .taskDescription(a.getTask().getDescription())
                .userId(a.getAssignedTo().getId())
                .username(a.getAssignedTo().getUsername())
                .firstName(a.getAssignedTo().getFirstName())
                .lastName(a.getAssignedTo().getLastName())
                .status(a.getStatus())
                .score(a.getScore())
                .attempts(a.getAttempts())
                .questionsJson(safeQuestions)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
