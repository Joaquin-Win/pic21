/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.JavaType
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.pic21.domain.AsignacionTarea
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.domain.EstadoTarea
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Rol
 *  com.pic21.domain.Tarea
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.request.TaskRequest
 *  com.pic21.dto.response.TaskAssignmentResponse
 *  com.pic21.dto.response.TaskResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.AsignacionTareaRepository
 *  com.pic21.repository.AsistenciaRepository
 *  com.pic21.repository.ReunionRepository
 *  com.pic21.repository.TareaRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.TaskService
 *  jakarta.persistence.EntityManager
 *  jakarta.persistence.PersistenceContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pic21.domain.AsignacionTarea;
import com.pic21.domain.EstadoReunion;
import com.pic21.domain.EstadoTarea;
import com.pic21.domain.Reunion;
import com.pic21.domain.Rol;
import com.pic21.domain.Tarea;
import com.pic21.domain.Usuario;
import com.pic21.dto.request.TaskRequest;
import com.pic21.dto.response.TaskAssignmentResponse;
import com.pic21.dto.response.TaskResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AsignacionTareaRepository;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.TareaRepository;
import com.pic21.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final TareaRepository tareaRepository;
    private final AsignacionTareaRepository asignacionTareaRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private static final Set<Rol> ASSIGNABLE_ROLES = new HashSet<Rol>(Arrays.asList(Rol.R02_ESTUDIANTE, Rol.R06_AYUDANTE, Rol.R03_EGRESADO, Rol.R07_ESTUDIANTE_POSGRADO));

    @Transactional
    public List<TaskAssignmentResponse> createForAbsent(Long reunionId, TaskRequest request, String creatorUsername) {
        Reunion reunion = (Reunion)this.reunionRepository.findById(reunionId).orElseThrow(() -> new ResourceNotFoundException("Reuni\u00f3n", reunionId));
        if (this.tareaRepository.existsById(reunionId)) {
            throw new BusinessException("La reuni\u00f3n '" + reunion.getTitulo() + "' ya tiene una tarea creada.");
        }
        if (reunion.getEstado() == EstadoReunion.NO_INICIADA) {
            throw new BusinessException("No se pueden crear tareas para una reuni\u00f3n NO_INICIADA.");
        }
        Usuario creator = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(creatorUsername).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + creatorUsername));
        List<Long> presentIds = this.asistenciaRepository.findByReunionWithDetails(reunion).stream().map(a -> a.getUsuario().getId()).collect(Collectors.toList());
        List<Usuario> absentees = this.usuarioRepository.findAll().stream().filter(u -> u.getRoles().stream().anyMatch(ASSIGNABLE_ROLES::contains)).filter(u -> !presentIds.contains(u.getId())).collect(Collectors.toList());
        if (absentees.isEmpty()) {
            throw new BusinessException("Todos los estudiantes y ayudantes asistieron a '" + reunion.getTitulo() + "'. Sin ausentes.");
        }
        Tarea tarea = Tarea.builder().reunion(reunion).titulo(request.getTitle()).descripcion(request.getDescription()).link(this.resolvePrimaryLink(request)).linksExtraJson(this.serializeLinks(request.getLinks())).questionsJson(request.getQuestionsJson()).estado(EstadoTarea.PENDIENTE).creadoPor(creator).build();
        Tarea savedTarea = tarea = (Tarea)this.tareaRepository.save(tarea);
        List<AsignacionTarea> toSave = absentees.stream().filter(u -> !this.asignacionTareaRepository.existsByTareaIdAndUsuarioId(savedTarea.getId(), u.getId())).map(u -> AsignacionTarea.builder().tarea(savedTarea).usuario(u).estado(EstadoTarea.PENDIENTE).build()).collect(Collectors.toList());
        if (toSave.isEmpty()) {
            throw new BusinessException("Ya existen asignaciones para todos los ausentes en esta tarea.");
        }
        List<AsignacionTarea> saved = this.asignacionTareaRepository.saveAll(toSave);
        log.info("Tarea id={} '{}' creada con {} asignaciones en reuni\u00f3n id={} por '{}'", new Object[]{savedTarea.getId(), request.getTitle(), saved.size(), reunionId, creatorUsername});
        return saved.stream().map(arg_0 -> this.mapAssignment(arg_0)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public List<TaskAssignmentResponse> findMyAssignments(String username) {
        Usuario usuario = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return this.asignacionTareaRepository.findByUsuarioId(usuario.getId()).stream().map(arg_0 -> this.mapAssignment(arg_0)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public List<TaskResponse> findAllByRole(String username) {
        Usuario usuario = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        boolean isAdmin = usuario.getRoles().contains(Rol.R04_ADMIN);
        List<Tarea> tareas = isAdmin ? this.tareaRepository.findAllWithDetails() : this.tareaRepository.findByCreadoPorIdOrderByCreatedAtDesc(usuario.getId());
        return tareas.stream().map(t -> this.mapTask(t, false)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public List<TaskAssignmentResponse> getAssignments(Long tareaId) {
        if (!this.tareaRepository.existsById(tareaId)) {
            throw new ResourceNotFoundException("Tarea", tareaId);
        }
        return this.asignacionTareaRepository.findByTareaIdWithUsuario(tareaId).stream().map(arg_0 -> this.mapAssignment(arg_0)).collect(Collectors.toList());
    }

    @Transactional
    public List<TaskAssignmentResponse> addUsersToTask(Long tareaId, List<Long> userIds) {
        Tarea tarea = (Tarea)this.tareaRepository.findById(tareaId).orElseThrow(() -> new ResourceNotFoundException("Tarea", tareaId));
        if (tarea.getEstado() == EstadoTarea.BLOQUEADA) {
            throw new BusinessException("No se pueden agregar usuarios a una tarea bloqueada.");
        }
        List<Usuario> usuarios = this.usuarioRepository.findAllById(userIds);
        if (usuarios.isEmpty()) {
            throw new BusinessException("No se encontraron usuarios con los IDs proporcionados.");
        }
        List<AsignacionTarea> toSave = usuarios.stream().filter(u -> !this.asignacionTareaRepository.existsByTareaIdAndUsuarioId(tareaId, u.getId())).map(u -> AsignacionTarea.builder().tarea(tarea).usuario(u).estado(EstadoTarea.PENDIENTE).build()).collect(Collectors.toList());
        if (toSave.isEmpty()) {
            throw new BusinessException("Todos los usuarios seleccionados ya est\u00e1n asignados a esta tarea.");
        }
        List<AsignacionTarea> saved = this.asignacionTareaRepository.saveAll(toSave);
        log.info("Tarea id={} '{}': {} usuario(s) agregado(s) manualmente", new Object[]{tareaId, tarea.getTitulo(), saved.size()});
        return saved.stream().map(arg_0 -> this.mapAssignment(arg_0)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public List<TaskResponse> findPendingByMeeting(Long reunionId) {
        if (!this.reunionRepository.existsById(reunionId)) {
            throw new ResourceNotFoundException("Reuni\u00f3n", reunionId);
        }
        List<Tarea> tareas = this.tareaRepository.findByReunionId(reunionId);
        return tareas.stream().filter(t -> this.asignacionTareaRepository.countByTareaIdAndEstado(t.getId(), EstadoTarea.PENDIENTE) > 0L).map(t -> this.mapTask(t, false)).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Tarea tarea = (Tarea)this.tareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        tarea.setTitulo(request.getTitle());
        tarea.setDescripcion(request.getDescription());
        tarea.setLink(this.resolvePrimaryLink(request));
        tarea.setLinksExtraJson(this.serializeLinks(request.getLinks()));
        tarea.setQuestionsJson(request.getQuestionsJson());
        log.info("Tarea id={} actualizada", id);
        return this.mapTask((Tarea)this.tareaRepository.save(tarea), false);
    }

    @Transactional
    public void deleteTask(Long id) {
        Tarea tarea = (Tarea)this.tareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        this.tareaRepository.delete(tarea);
        log.info("Tarea id={} '{}' eliminada", id, tarea.getTitulo());
    }

    @Transactional
    public TaskResponse blockTask(Long id) {
        Tarea tarea = (Tarea)this.tareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        if (tarea.getEstado() == EstadoTarea.BLOQUEADA) {
            throw new BusinessException("La tarea ya est\u00e1 bloqueada.");
        }
        tarea.setEstado(EstadoTarea.BLOQUEADA);
        this.tareaRepository.save(tarea);
        int updated = this.asignacionTareaRepository.updateEstadoByTareaIdAndEstado(id, EstadoTarea.PENDIENTE, EstadoTarea.BLOQUEADA);
        log.info("Tarea id={} bloqueada. Asignaciones pendientes bloqueadas={}", id, updated);
        return this.mapTask(tarea, false);
    }

    @Transactional
    public TaskResponse unblockTask(Long id) {
        Tarea tarea = (Tarea)this.tareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        if (tarea.getEstado() != EstadoTarea.BLOQUEADA) {
            throw new BusinessException("La tarea no est\u00e1 bloqueada.");
        }
        tarea.setEstado(EstadoTarea.PENDIENTE);
        this.tareaRepository.save(tarea);
        int updated = this.asignacionTareaRepository.updateEstadoByTareaIdAndEstado(id, EstadoTarea.BLOQUEADA, EstadoTarea.PENDIENTE);
        log.info("Tarea id={} desbloqueada. Asignaciones reactivadas={}", id, updated);
        return this.mapTask(tarea, false);
    }

    @Transactional
    public TaskAssignmentResponse changeAssignmentStatus(Long assignmentId, String estadoStr) {
        EstadoTarea newEstado;
        AsignacionTarea asignacion = (AsignacionTarea)this.asignacionTareaRepository.findById(assignmentId).orElseThrow(() -> new ResourceNotFoundException("Asignaci\u00f3n", assignmentId));
        try {
            newEstado = EstadoTarea.valueOf((String)estadoStr.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inv\u00e1lido: " + estadoStr + ". V\u00e1lidos: PENDIENTE, COMPLETADA, BLOQUEADA");
        }
        if (newEstado == EstadoTarea.COMPLETADA) {
            asignacion.setFechaCompletado(LocalDateTime.now());
            if (asignacion.getScore() == null || asignacion.getScore() < 100) {
                asignacion.setScore(Integer.valueOf(100));
            }
            Long reunionId = asignacion.getTarea().getReunion().getId();
            String reunionTitulo = asignacion.getTarea().getReunion().getTitulo();
            Long userId = asignacion.getUsuario().getId();
            String username = asignacion.getUsuario().getUsername();
            this.registerAutoAttendance(reunionId, userId, username, reunionTitulo);
        }
        asignacion.setEstado(newEstado);
        log.info("Asignaci\u00f3n id={} \u2192 estado {}{}", new Object[]{assignmentId, newEstado, newEstado == EstadoTarea.COMPLETADA ? " (manual por ADMIN, asistencia registrada)" : ""});
        return this.mapAssignment((AsignacionTarea)this.asignacionTareaRepository.save(asignacion));
    }

    @Transactional
    public TaskAssignmentResponse submitQuiz(Long assignmentId, List<Integer> answers, String username) {
        List<Map<String, Object>> questions;
        AsignacionTarea asignacion = (AsignacionTarea)this.asignacionTareaRepository.findById(assignmentId).orElseThrow(() -> new ResourceNotFoundException("Asignaci\u00f3n", assignmentId));
        Usuario usuario = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(username).or(() -> this.usuarioRepository.findByCredencial_EmailIgnoreCase(username)).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        if (!asignacion.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Esta asignaci\u00f3n no te pertenece.");
        }
        if (asignacion.getTarea().getEstado() == EstadoTarea.BLOQUEADA || asignacion.getEstado() == EstadoTarea.BLOQUEADA) {
            throw new BusinessException("Esta tarea est\u00e1 bloqueada. Ya no se puede recuperar asistencia.");
        }
        if (asignacion.getEstado() == EstadoTarea.COMPLETADA) {
            throw new BusinessException("Ya completaste este quiz.");
        }
        String questionsJson = asignacion.getTarea().getQuestionsJson();
        if (questionsJson == null || questionsJson.isBlank()) {
            throw new BusinessException("Esta tarea no tiene un quiz configurado.");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            questions = (List<Map<String, Object>>)(List<?>)mapper.readValue(questionsJson, List.class);
        }
        catch (Exception e) {
            throw new BusinessException("Error al leer las preguntas del quiz.");
        }
        if (answers == null || answers.size() != questions.size()) {
            throw new BusinessException("Deb\u00e9s responder todas las preguntas (" + questions.size() + ").");
        }
        int correct = 0;
        for (int i = 0; i < questions.size(); ++i) {
            int expected;
            Map<String, Object> q = questions.get(i);
            Object correctIdx = q.get("correct");
            int n = expected = correctIdx instanceof Number ? ((Number)correctIdx).intValue() : -1;
            if (answers.get(i) == null || answers.get(i) != expected) continue;
            ++correct;
        }
        int scorePercent = (int)Math.round((double)correct * 100.0 / (double)questions.size());
        int currentAttempts = asignacion.getAttempts();
        EstadoTarea newEstado = scorePercent >= 70 ? EstadoTarea.COMPLETADA : EstadoTarea.PENDIENTE;
        Long reunionId = asignacion.getTarea().getReunion().getId();
        String reunionTitulo = asignacion.getTarea().getReunion().getTitulo();
        Long userId = usuario.getId();
        this.asignacionTareaRepository.updateQuizResult(assignmentId, Integer.valueOf(scorePercent), currentAttempts + 1, newEstado);
        log.info("Quiz {}: asignacion={}, user='{}', score={}%, intento #{}", new Object[]{newEstado == EstadoTarea.COMPLETADA ? "COMPLETADO" : "PENDIENTE", assignmentId, username, scorePercent, currentAttempts + 1});
        if (newEstado == EstadoTarea.COMPLETADA) {
            this.registerAutoAttendance(reunionId, userId, username, reunionTitulo);
            asignacion.setFechaCompletado(LocalDateTime.now());
        }
        asignacion.setScore(Integer.valueOf(scorePercent));
        asignacion.setAttempts(currentAttempts + 1);
        asignacion.setEstado(newEstado);
        return this.mapAssignment(asignacion);
    }

    private void registerAutoAttendance(Long reunionId, Long userId, String username, String reunionTitulo) {
        try {
            this.entityManager.createNativeQuery("INSERT INTO asistencias (reunion_id, usuario_id, fecha_registro, presente) VALUES (:reunionId, :userId, NOW(), true) ON CONFLICT (reunion_id, usuario_id) DO NOTHING").setParameter("reunionId", reunionId).setParameter("userId", userId).executeUpdate();
            log.info("Asistencia auto-registrada: user='{}', reunion='{}'", username, reunionTitulo);
        }
        catch (Exception ex) {
            log.warn("No se pudo auto-registrar asistencia para user='{}': {}", username, ex.getMessage());
        }
    }

    private TaskResponse mapTask(Tarea t, boolean includeAssignments) {
        long total = this.asignacionTareaRepository.countByTareaId(t.getId());
        long pending = this.asignacionTareaRepository.countByTareaIdAndEstado(t.getId(), EstadoTarea.PENDIENTE);
        return TaskResponse.builder().id(t.getId()).reunionId(t.getReunion().getId()).reunionTitulo(t.getReunion().getTitulo()).titulo(t.getTitulo()).descripcion(t.getDescripcion()).link(t.getLink()).links(this.deserializeLinks(t.getLinksExtraJson(), t.getLink())).questionsJson(t.getQuestionsJson()).estado(t.getEstado()).creadoPorId(t.getCreadoPor().getId()).creadoPorUsername(t.getCreadoPor().getUsername()).createdAt(t.getCreatedAt()).totalAsignaciones(total).pendientes(pending).build();
    }

    private TaskAssignmentResponse mapAssignment(AsignacionTarea a) {
        String safeQuestions = null;
        String raw = a.getTarea().getQuestionsJson();
        if (raw != null && !raw.isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> qs = (List<Map<String, Object>>)(List<?>)mapper.readValue(raw, (JavaType)mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                qs.forEach(q -> q.remove("correct"));
                safeQuestions = mapper.writeValueAsString(qs);
            }
            catch (Exception e) {
                safeQuestions = raw;
            }
        }
        return TaskAssignmentResponse.builder().id(a.getId()).tareaId(a.getTarea().getId()).tituloTarea(a.getTarea().getTitulo()).reunionId(a.getTarea().getReunion() != null ? a.getTarea().getReunion().getId() : null).reunionTitulo(a.getTarea().getReunion() != null ? a.getTarea().getReunion().getTitulo() : null).descripcionTarea(a.getTarea().getDescripcion()).linkTarea(a.getTarea().getLink()).linksTarea(this.deserializeLinks(a.getTarea().getLinksExtraJson(), a.getTarea().getLink())).usuarioId(a.getUsuario().getId()).username(a.getUsuario().getUsername()).nombre(a.getUsuario().getNombre()).apellido(a.getUsuario().getApellido()).estado(a.getEstado()).score(a.getScore()).intentos(a.getAttempts()).questionsJson(safeQuestions).fechaAsignacion(a.getFechaAsignacion()).fechaCompletado(a.getFechaCompletado()).build();
    }

    private String resolvePrimaryLink(TaskRequest request) {
        if (request.getLink() != null && !request.getLink().isBlank()) {
            return request.getLink().trim();
        }
        if (request.getLinks() != null) {
            return request.getLinks().stream().filter(l -> l != null && !l.isBlank()).map(String::trim).findFirst().orElse(null);
        }
        return null;
    }

    private String serializeLinks(List<String> links) {
        if (links == null || links.isEmpty()) {
            return "[]";
        }
        List<String> cleaned = links.stream().filter(l -> l != null && !l.isBlank()).map(String::trim).distinct().collect(Collectors.toList());
        try {
            return MAPPER.writeValueAsString(cleaned);
        }
        catch (Exception e) {
            return "[]";
        }
    }

    private List<String> deserializeLinks(String linksJson, String fallbackLink) {
        try {
            List<String> parsed;
            List<String> cleaned;
            if (linksJson != null && !linksJson.isBlank() && !(cleaned = (parsed = (List<String>)(List<?>)MAPPER.readValue(linksJson, (JavaType)MAPPER.getTypeFactory().constructCollectionType(List.class, String.class))).stream().filter(l -> l != null && !l.isBlank()).map(String::trim).distinct().collect(Collectors.toList())).isEmpty()) {
                return cleaned;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (fallbackLink != null && !fallbackLink.isBlank()) {
            return List.of(fallbackLink.trim());
        }
        return List.of();
    }

    public TaskService(TareaRepository tareaRepository, AsignacionTareaRepository asignacionTareaRepository, ReunionRepository reunionRepository, UsuarioRepository usuarioRepository, AsistenciaRepository asistenciaRepository) {
        this.tareaRepository = tareaRepository;
        this.asignacionTareaRepository = asignacionTareaRepository;
        this.reunionRepository = reunionRepository;
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
    }
}

