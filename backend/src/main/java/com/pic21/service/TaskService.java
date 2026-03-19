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
 * Servicio de tareas para el módulo de gestión PIC21.
 *
 * Regla de negocio principal:
 *   Al crear tareas para una reunión, el sistema asigna automáticamente una tarea
 *   a CADA ESTUDIANTE que NO registró asistencia en esa reunión.
 *
 * Permisos:
 *   - Crear tareas: PROFESOR o AYUDANTE
 *   - Ver mis tareas: cualquier usuario autenticado (solo ve las suyas)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    // -----------------------------------------------------------------------
    // CREAR tareas — asignación automática a ausentes de la reunión
    // -----------------------------------------------------------------------

    /**
     * Crea y asigna una tarea a todos los estudiantes que NO registraron asistencia
     * en la reunión indicada.
     *
     * Validaciones:
     *   - La reunión debe existir.
     *   - La reunión debe estar BLOQUEADA o ACTIVA (no tiene sentido en NO_INICIADA).
     *   - El creador debe ser encontrado por username (viene del JWT).
     *   - Si un estudiante ya tiene tarea asignada para esa reunión, se omite (no se duplica).
     *
     * @param meetingId ID de la reunión
     * @param request   DTO con título, descripción y link de la tarea
     * @param creatorUsername username del usuario autenticado (PROFESOR/AYUDANTE)
     * @return lista de tareas creadas (una por estudiante ausente)
     */
    @Transactional
    public List<TaskResponse> createForAbsent(Long meetingId, TaskRequest request, String creatorUsername) {
        // 1. Buscar la reunión
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));

        // 2. Validar que la reunión no esté en NO_INICIADA (no tiene asistencias todavía)
        if (meeting.getStatus() == MeetingStatus.NO_INICIADA) {
            throw new BusinessException(
                    "No se pueden crear tareas para una reunión que aún no ha iniciado (NO_INICIADA). " +
                    "La reunión debe estar ACTIVA o BLOQUEADA.");
        }

        // 3. Buscar al creador
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + creatorUsername));

        // 4. Obtener IDs de usuarios que SÍ asistieron
        List<Long> presentUserIds = attendanceRepository.findByMeetingWithDetails(meeting)
                .stream()
                .map(a -> a.getUser().getId())
                .collect(Collectors.toList());

        // 5. Obtener todos los estudiantes del sistema
        List<User> allStudents = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName() == RoleName.ESTUDIANTE))
                .collect(Collectors.toList());

        if (allStudents.isEmpty()) {
            throw new BusinessException("No hay estudiantes registrados en el sistema.");
        }

        // 6. Filtrar ausentes (no en la lista de presentes)
        List<User> absentStudents = allStudents.stream()
                .filter(u -> !presentUserIds.contains(u.getId()))
                .collect(Collectors.toList());

        if (absentStudents.isEmpty()) {
            throw new BusinessException(
                    "Todos los estudiantes asistieron a la reunión '" + meeting.getTitle() +
                    "'. No hay ausentes para asignar tareas.");
        }

        // 7. Crear una tarea por cada ausente (ignorar si ya tiene tarea en esta reunión)
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
                    "Todos los estudiantes ausentes ya tienen una tarea asignada para la reunión '" +
                    meeting.getTitle() + "'.");
        }

        List<Task> saved = taskRepository.saveAll(createdTasks);
        log.info("Tareas creadas: {} tareas para reunión id={} por '{}'",
                saved.size(), meetingId, creatorUsername);

        return saved.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // MIS TAREAS — cualquier usuario autenticado ve solo las propias
    // -----------------------------------------------------------------------

    /**
     * Retorna todas las tareas asignadas al usuario autenticado.
     *
     * @param username username del usuario autenticado
     * @return lista de sus tareas, ordenadas por fecha de creación descendente
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> findMyTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        return taskRepository.findByAssignedToId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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
