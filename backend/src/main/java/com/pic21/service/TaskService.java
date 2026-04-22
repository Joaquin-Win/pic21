package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskAssignmentResponse;
import com.pic21.dto.response.TaskResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de tareas (UML v8).
 *
 * Tarea tiene composición 1:1 con Reunion (@MapsId).
 * AsignacionTarea es N:1 con Tarea y N:1 con Usuario.
 * EstadoTarea: PENDIENTE, COMPLETADA, BLOQUEADA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private final TareaRepository tareaRepository;
    private final AsignacionTareaRepository asignacionTareaRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Roles que pueden recibir asignaciones
    private static final Set<Rol> ASSIGNABLE_ROLES = new HashSet<>(Arrays.asList(
            Rol.R02_ESTUDIANTE, Rol.R06_AYUDANTE, Rol.R03_EGRESADO
    ));

    // ── Crear tarea + asignaciones para ausentes ───────────────

    @Transactional
    public List<TaskAssignmentResponse> createForAbsent(Long reunionId, TaskRequest request, String creatorUsername) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", reunionId));

        if (tareaRepository.existsById(reunionId)) {
            throw new BusinessException("La reunión '" + reunion.getTitulo() + "' ya tiene una tarea creada.");
        }

        if (reunion.getEstado() == EstadoReunion.NO_INICIADA) {
            throw new BusinessException("No se pueden crear tareas para una reunión NO_INICIADA.");
        }

        Usuario creator = usuarioRepository.findByUsernameIgnoreCase(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + creatorUsername));

        // IDs que SÍ asistieron
        List<Long> presentIds = asistenciaRepository.findByReunionWithDetails(reunion)
                .stream().map(a -> a.getUsuario().getId()).collect(Collectors.toList());

        // Ausentes con roles asignables
        List<Usuario> absentees = usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(ASSIGNABLE_ROLES::contains))
                .filter(u -> !presentIds.contains(u.getId()))
                .collect(Collectors.toList());

        if (absentees.isEmpty()) {
            throw new BusinessException(
                    "Todos los estudiantes y ayudantes asistieron a '" + reunion.getTitulo() + "'. Sin ausentes.");
        }

        // Crear Tarea con @MapsId (id = reunion.id)
        Tarea tarea = Tarea.builder()
                .reunion(reunion)
                .titulo(request.getTitle())
                .descripcion(request.getDescription())
                .link(resolvePrimaryLink(request))
                .linksExtraJson(serializeLinks(request.getLinks()))
                .questionsJson(request.getQuestionsJson())
                .estado(EstadoTarea.PENDIENTE)
                .creadoPor(creator)
                .build();
        tarea = tareaRepository.save(tarea);

        final Tarea savedTarea = tarea;
        List<AsignacionTarea> toSave = absentees.stream()
                .filter(u -> !asignacionTareaRepository.existsByTareaIdAndUsuarioId(savedTarea.getId(), u.getId()))
                .map(u -> AsignacionTarea.builder()
                        .tarea(savedTarea)
                        .usuario(u)
                        .estado(EstadoTarea.PENDIENTE)
                        .build())
                .collect(Collectors.toList());

        if (toSave.isEmpty()) {
            throw new BusinessException("Ya existen asignaciones para todos los ausentes en esta tarea.");
        }

        List<AsignacionTarea> saved = asignacionTareaRepository.saveAll(toSave);
        log.info("Tarea id={} '{}' creada con {} asignaciones en reunión id={} por '{}'",
                savedTarea.getId(), request.getTitle(), saved.size(), reunionId, creatorUsername);

        return saved.stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    // ── Mis asignaciones ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> findMyAssignments(String username) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return asignacionTareaRepository.findByUsuarioId(usuario.getId())
                .stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    // ── Todas las tareas según rol ─────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskResponse> findAllByRole(String username) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        boolean isAdmin = usuario.getRoles().contains(Rol.R04_ADMIN);

        List<Tarea> tareas = isAdmin
                ? tareaRepository.findAllWithDetails()
                : tareaRepository.findByCreadoPorIdOrderByCreatedAtDesc(usuario.getId());

        return tareas.stream().map(t -> mapTask(t, false)).collect(Collectors.toList());
    }

    // ── Asignaciones de una tarea ──────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> getAssignments(Long tareaId) {
        if (!tareaRepository.existsById(tareaId)) throw new ResourceNotFoundException("Tarea", tareaId);
        return asignacionTareaRepository.findByTareaIdWithUsuario(tareaId)
                .stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    // ── Pendientes por reunión ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaskResponse> findPendingByMeeting(Long reunionId) {
        if (!reunionRepository.existsById(reunionId)) throw new ResourceNotFoundException("Reunión", reunionId);
        List<Tarea> tareas = tareaRepository.findByReunionId(reunionId);
        return tareas.stream()
                .filter(t -> asignacionTareaRepository.countByTareaIdAndEstado(t.getId(), EstadoTarea.PENDIENTE) > 0)
                .map(t -> mapTask(t, false))
                .collect(Collectors.toList());
    }

    // ── Editar tarea ───────────────────────────────────────────

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        tarea.setTitulo(request.getTitle());
        tarea.setDescripcion(request.getDescription());
        tarea.setLink(resolvePrimaryLink(request));
        tarea.setLinksExtraJson(serializeLinks(request.getLinks()));
        tarea.setQuestionsJson(request.getQuestionsJson());
        log.info("Tarea id={} actualizada", id);
        return mapTask(tareaRepository.save(tarea), false);
    }

    // ── Eliminar tarea ─────────────────────────────────────────

    @Transactional
    public void deleteTask(Long id) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        tareaRepository.delete(tarea);
        log.info("Tarea id={} '{}' eliminada", id, tarea.getTitulo());
    }

    @Transactional
    public TaskResponse blockTask(Long id) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));

        if (tarea.getEstado() == EstadoTarea.BLOQUEADA) {
            throw new BusinessException("La tarea ya está bloqueada.");
        }

        tarea.setEstado(EstadoTarea.BLOQUEADA);
        tareaRepository.save(tarea);

        int updated = asignacionTareaRepository.updateEstadoByTareaIdAndEstado(
                id, EstadoTarea.PENDIENTE, EstadoTarea.BLOQUEADA
        );
        log.info("Tarea id={} bloqueada. Asignaciones pendientes bloqueadas={}", id, updated);
        return mapTask(tarea, false);
    }

    // ── Cambiar estado de asignación ───────────────────────────

    @Transactional
    public TaskAssignmentResponse changeAssignmentStatus(Long assignmentId, String estadoStr) {
        AsignacionTarea asignacion = asignacionTareaRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignmentId));
        EstadoTarea newEstado;
        try {
            newEstado = EstadoTarea.valueOf(estadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inválido: " + estadoStr + ". Válidos: PENDIENTE, COMPLETADA, BLOQUEADA");
        }
        if (newEstado == EstadoTarea.COMPLETADA) {
            asignacion.setFechaCompletado(LocalDateTime.now());
        }
        asignacion.setEstado(newEstado);
        log.info("Asignación id={} → estado {}", assignmentId, newEstado);
        return mapAssignment(asignacionTareaRepository.save(asignacion));
    }

    // ── Submit quiz ────────────────────────────────────────────

    @Transactional
    public TaskAssignmentResponse submitQuiz(Long assignmentId, List<Integer> answers, String username) {
        AsignacionTarea asignacion = asignacionTareaRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación", assignmentId));

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .or(() -> usuarioRepository.findByCredencial_EmailIgnoreCase(username))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        if (!asignacion.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Esta asignación no te pertenece.");
        }

        if (asignacion.getTarea().getEstado() == EstadoTarea.BLOQUEADA || asignacion.getEstado() == EstadoTarea.BLOQUEADA) {
            throw new BusinessException("Esta tarea está bloqueada. Ya no se puede recuperar asistencia.");
        }

        if (asignacion.getEstado() == EstadoTarea.COMPLETADA) {
            throw new BusinessException("Ya completaste este quiz.");
        }

        String questionsJson = asignacion.getTarea().getQuestionsJson();
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
        int currentAttempts = asignacion.getAttempts();
        EstadoTarea newEstado = scorePercent >= 70 ? EstadoTarea.COMPLETADA : EstadoTarea.PENDIENTE;
        Long reunionId = asignacion.getTarea().getReunion().getId();
        String reunionTitulo = asignacion.getTarea().getReunion().getTitulo();
        Long userId = usuario.getId();

        asignacionTareaRepository.updateQuizResult(assignmentId, scorePercent, currentAttempts + 1, newEstado);

        log.info("Quiz {}: asignacion={}, user='{}', score={}%, intento #{}",
                newEstado == EstadoTarea.COMPLETADA ? "COMPLETADO" : "PENDIENTE",
                assignmentId, username, scorePercent, currentAttempts + 1);

        if (newEstado == EstadoTarea.COMPLETADA) {
            registerAutoAttendance(reunionId, userId, username, reunionTitulo);
            asignacion.setFechaCompletado(LocalDateTime.now());
        }

        asignacion.setScore(scorePercent);
        asignacion.setAttempts(currentAttempts + 1);
        asignacion.setEstado(newEstado);
        return mapAssignment(asignacion);
    }

    private void registerAutoAttendance(Long reunionId, Long userId, String username, String reunionTitulo) {
        try {
            entityManager.createNativeQuery(
                    "INSERT INTO asistencias (reunion_id, usuario_id, fecha_registro, presente) " +
                    "VALUES (:reunionId, :userId, NOW(), true) " +
                    "ON CONFLICT (reunion_id, usuario_id) DO NOTHING")
                    .setParameter("reunionId", reunionId)
                    .setParameter("userId", userId)
                    .executeUpdate();
            log.info("Asistencia auto-registrada: user='{}', reunion='{}'", username, reunionTitulo);
        } catch (Exception ex) {
            log.warn("No se pudo auto-registrar asistencia para user='{}': {}", username, ex.getMessage());
        }
    }

    // ── Mappers ────────────────────────────────────────────────

    private TaskResponse mapTask(Tarea t, boolean includeAssignments) {
        long total    = asignacionTareaRepository.countByTareaId(t.getId());
        long pending  = asignacionTareaRepository.countByTareaIdAndEstado(t.getId(), EstadoTarea.PENDIENTE);
        return TaskResponse.builder()
                .id(t.getId())
                .reunionId(t.getReunion().getId())
                .reunionTitulo(t.getReunion().getTitulo())
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .link(t.getLink())
                .links(deserializeLinks(t.getLinksExtraJson(), t.getLink()))
                .questionsJson(t.getQuestionsJson())
                .estado(t.getEstado())
                .creadoPorId(t.getCreadoPor().getId())
                .creadoPorUsername(t.getCreadoPor().getUsername())
                .createdAt(t.getCreatedAt())
                .totalAsignaciones(total)
                .pendientes(pending)
                .build();
    }

    private TaskAssignmentResponse mapAssignment(AsignacionTarea a) {
        // Quitar respuestas correctas para no hacer trampa
        String safeQuestions = null;
        String raw = a.getTarea().getQuestionsJson();
        if (raw != null && !raw.isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                List<java.util.Map<String, Object>> qs = mapper.readValue(raw,
                        mapper.getTypeFactory().constructCollectionType(List.class, java.util.Map.class));
                qs.forEach(q -> q.remove("correct"));
                safeQuestions = mapper.writeValueAsString(qs);
            } catch (Exception e) {
                safeQuestions = raw;
            }
        }

        return TaskAssignmentResponse.builder()
                .id(a.getId())
                .tareaId(a.getTarea().getId())
                .tituloTarea(a.getTarea().getTitulo())
                .reunionId(a.getTarea().getReunion() != null ? a.getTarea().getReunion().getId() : null)
                .reunionTitulo(a.getTarea().getReunion() != null ? a.getTarea().getReunion().getTitulo() : null)
                .descripcionTarea(a.getTarea().getDescripcion())
                .linkTarea(a.getTarea().getLink())
                .linksTarea(deserializeLinks(a.getTarea().getLinksExtraJson(), a.getTarea().getLink()))
                .usuarioId(a.getUsuario().getId())
                .username(a.getUsuario().getUsername())
                .nombre(a.getUsuario().getNombre())
                .apellido(a.getUsuario().getApellido())
                .estado(a.getEstado())
                .score(a.getScore())
                .intentos(a.getAttempts())
                .questionsJson(safeQuestions)
                .fechaAsignacion(a.getFechaAsignacion())
                .fechaCompletado(a.getFechaCompletado())
                .build();
    }

    private String resolvePrimaryLink(TaskRequest request) {
        if (request.getLink() != null && !request.getLink().isBlank()) {
            return request.getLink().trim();
        }
        if (request.getLinks() != null) {
            return request.getLinks().stream()
                    .filter(l -> l != null && !l.isBlank())
                    .map(String::trim)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private String serializeLinks(List<String> links) {
        if (links == null || links.isEmpty()) return "[]";
        List<String> cleaned = links.stream()
                .filter(l -> l != null && !l.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        try {
            return MAPPER.writeValueAsString(cleaned);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> deserializeLinks(String linksJson, String fallbackLink) {
        try {
            if (linksJson != null && !linksJson.isBlank()) {
                List<String> parsed = MAPPER.readValue(linksJson,
                        MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
                List<String> cleaned = parsed.stream()
                        .filter(l -> l != null && !l.isBlank())
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());
                if (!cleaned.isEmpty()) return cleaned;
            }
        } catch (Exception ignored) {
        }
        if (fallbackLink != null && !fallbackLink.isBlank()) {
            return List.of(fallbackLink.trim());
        }
        return List.of();
    }
}
