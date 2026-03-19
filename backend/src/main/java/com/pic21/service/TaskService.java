package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.domain.Role.RoleName;
import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AttendanceRepository;
import com.pic21.repository.MeetingRepository;
import com.pic21.repository.TaskRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de tareas para PIC21.
 *
 * Lógica de roles:
 *   ADMIN    → ve y gestiona TODAS las tareas
 *   PROFESOR → ve las tareas que él creó; puede crear nuevas
 *   AYUDANTE → ve las tareas asignadas a él (como ESTUDIANTE); NO puede crear
 *   ESTUDIANTE → ve las tareas asignadas a él
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    // ── Crear tareas para ausentes de una reunión ──────────
    @Transactional
    public List<TaskResponse> createForAbsent(Long meetingId, TaskRequest request, String creatorUsername) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));

        if (meeting.getStatus() == MeetingStatus.NO_INICIADA) {
            throw new BusinessException(
                    "No se pueden crear tareas para una reunión NO_INICIADA. Debe estar ACTIVA o BLOQUEADA.");
        }

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + creatorUsername));

        // IDs de usuarios que SÍ asistieron
        List<Long> presentUserIds = attendanceRepository.findByMeetingWithDetails(meeting)
                .stream()
                .map(a -> a.getUser().getId())
                .collect(Collectors.toList());

        // Todos los estudiantes del sistema
        List<User> allStudents = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ESTUDIANTE))
                .collect(Collectors.toList());

        if (allStudents.isEmpty()) {
            throw new BusinessException("No hay estudiantes registrados en el sistema.");
        }

        // Ausentes = estudiantes que NO asistieron
        List<User> absentStudents = allStudents.stream()
                .filter(u -> !presentUserIds.contains(u.getId()))
                .collect(Collectors.toList());

        if (absentStudents.isEmpty()) {
            throw new BusinessException(
                    "Todos los estudiantes asistieron a '" + meeting.getTitle() + "'. Sin ausentes para asignar tareas.");
        }

        // Crear una tarea por ausente (sin duplicar)
        List<Task> createdTasks = absentStudents.stream()
                .filter(student -> !taskRepository.existsByMeetingIdAndAssignedToId(meetingId, student.getId()))
                .map(student -> Task.builder()
                        .meeting(meeting)
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .link(request.getLink())
                        .assignedTo(student)
                        .createdBy(creator)
                        .status(TaskStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        if (createdTasks.isEmpty()) {
            throw new BusinessException(
                    "Todos los estudiantes ausentes ya tienen una tarea asignada para '" + meeting.getTitle() + "'.");
        }

        List<Task> saved = taskRepository.saveAll(createdTasks);
        log.info("Tareas creadas: {} para reunión id={} por '{}'", saved.size(), meetingId, creatorUsername);
        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Mis tareas asignadas ───────────────────────────────
    @Transactional(readOnly = true)
    public List<TaskResponse> findMyTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return taskRepository.findByAssignedToId(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Todas las tareas según rol ─────────────────────────
    @Transactional(readOnly = true)
    public List<TaskResponse> findAllByRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ADMIN);

        if (isAdmin) {
            // ADMIN ve todas
            return taskRepository.findAllWithDetails()
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        } else {
            // PROFESOR ve las que creó
            return taskRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId())
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }
    }

    // ── Pendientes por reunión ─────────────────────────────
    @Transactional(readOnly = true)
    public List<TaskResponse> findPendingByMeeting(Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Reunión", meetingId);
        }
        return taskRepository.findByMeetingIdAndStatus(meetingId, TaskStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Editar tarea (ADMIN) ───────────────────────────────
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setLink(request.getLink());
        log.info("Tarea id={} actualizada", id);
        return mapToResponse(taskRepository.save(task));
    }

    // ── Eliminar tarea (ADMIN) ─────────────────────────────
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        taskRepository.delete(task);
        log.info("Tarea id={} eliminada", id);
    }

    // ── Cambiar estado (ADMIN) ─────────────────────────────
    @Transactional
    public TaskResponse changeStatus(Long id, String statusStr) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inválido: " + statusStr + ". Válidos: PENDING, COMPLETED, CORRECTED");
        }
        task.setStatus(newStatus);
        log.info("Tarea id={} estado cambiado a {}", id, newStatus);
        return mapToResponse(taskRepository.save(task));
    }

    // ── Helpers ────────────────────────────────────────────
    private TaskResponse mapToResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .meetingId(t.getMeeting().getId())
                .meetingTitle(t.getMeeting().getTitle())
                .title(t.getTitle())
                .description(t.getDescription())
                .link(t.getLink())
                .assignedToId(t.getAssignedTo().getId())
                .assignedToUsername(t.getAssignedTo().getUsername())
                .assignedToFirstName(t.getAssignedTo().getFirstName())
                .assignedToLastName(t.getAssignedTo().getLastName())
                .createdById(t.getCreatedBy().getId())
                .createdByUsername(t.getCreatedBy().getUsername())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
