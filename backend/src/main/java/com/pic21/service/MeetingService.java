package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.dto.request.MeetingRequest;
import com.pic21.dto.response.MeetingResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de reuniones (UML v8).
 *
 * Transiciones de EstadoReunion:
 *   NO_INICIADA → EN_CURSO   : abrir registro de asistencia
 *   EN_CURSO    → BLOQUEADA  : cerrar registro de asistencia
 *   BLOQUEADA   → EN_CURSO   : solo ADMIN puede reactivar
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MeetingResponse create(MeetingRequest request, String username) {
        Usuario creator = findUsuarioOrThrow(username);

        Reunion reunion = Reunion.builder()
                .titulo(request.getTitle())
                .descripcion(request.getDescription())
                .fechaInicio(request.getScheduledAt())
                .estado(EstadoReunion.NO_INICIADA)
                .accessCode(request.getAccessCode())
                .recordingLink(request.getRecordingLink())
                .newsLink(request.getNewsLink())
                .activityLink(request.getActivityLink())
                .presentacionLink(request.getPresentacionLink())
                .linksExtraJson(serializeLinks(request.getLinksExtra()))
                .newsLinksExtraJson(serializeLinks(request.getNewsLinksExtra()))
                .creadoPor(creator)
                .build();

        Reunion saved = reunionRepository.save(reunion);
        log.info("Reunión creada: id={}, título='{}', por='{}'", saved.getId(), saved.getTitulo(), username);
        return mapToResponse(saved);
    }

    @Transactional
    public MeetingResponse update(Long id, MeetingRequest request) {
        Reunion reunion = findOrThrow(id);
        validateNotBlocked(reunion);

        reunion.setTitulo(request.getTitle());
        reunion.setDescripcion(request.getDescription());
        reunion.setFechaInicio(request.getScheduledAt());
        reunion.setAccessCode(request.getAccessCode());
        reunion.setRecordingLink(request.getRecordingLink());
        reunion.setNewsLink(request.getNewsLink());
        reunion.setActivityLink(request.getActivityLink());
        reunion.setPresentacionLink(request.getPresentacionLink());
        reunion.setLinksExtraJson(serializeLinks(request.getLinksExtra()));
        reunion.setNewsLinksExtraJson(serializeLinks(request.getNewsLinksExtra()));

        log.info("Reunión actualizada: id={}", id);
        return mapToResponse(reunionRepository.save(reunion));
    }

    @Transactional
    public void delete(Long id) {
        if (!reunionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reunión", id);
        }
        entityManager.createNativeQuery("DELETE FROM asistencias WHERE reunion_id = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM asignaciones_tarea WHERE tarea_id IN (SELECT id FROM tareas WHERE reunion_id = :id)")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tareas WHERE reunion_id = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM meeting_files WHERE meeting_id = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM reuniones WHERE id = :id")
                .setParameter("id", id).executeUpdate();
        log.info("Reunión id={} eliminada con asistencias, tareas y archivos", id);
    }

    @Transactional
    public MeetingResponse changeStatus(Long id, EstadoReunion newEstado, boolean isAdmin) {
        Reunion reunion = findOrThrow(id);

        // Solo ADMIN puede desbloquear
        if (reunion.getEstado() == EstadoReunion.BLOQUEADA
                && newEstado == EstadoReunion.EN_CURSO
                && !isAdmin) {
            throw new BusinessException("Solo ADMIN puede desbloquear reuniones.");
        }

        validateStatusTransition(reunion.getEstado(), newEstado);

        EstadoReunion previous = reunion.getEstado();
        reunion.setEstado(newEstado);

        log.info("Reunión id={} cambió estado: {} → {}", id, previous, newEstado);
        return mapToResponse(reunionRepository.save(reunion));
    }

    @Transactional
    public MeetingResponse changeStatus(Long id, EstadoReunion newEstado) {
        return changeStatus(id, newEstado, false);
    }

    @Transactional(readOnly = true)
    public Page<MeetingResponse> findAll(Pageable pageable) {
        return reunionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MeetingResponse findById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Transactional
    public MeetingResponse uploadPdf(Long id, MultipartFile file) {
        Reunion reunion = findOrThrow(id);
        validateNotBlocked(reunion);

        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                throw new BusinessException("El archivo debe ser un PDF válido.");
            }
            reunion.setPdfFileData(file.getBytes());
            reunion.setPdfFileName(StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento.pdf"));
        } catch (IOException e) {
            throw new BusinessException("Error al procesar el archivo PDF: " + e.getMessage());
        }

        log.info("PDF subido para reunión id={}", id);
        return mapToResponse(reunionRepository.save(reunion));
    }

    @Transactional(readOnly = true)
    public Reunion getReunionWithPdf(Long id) {
        Reunion reunion = findOrThrow(id);
        if (reunion.getPdfFileData() == null) {
            throw new ResourceNotFoundException("Archivo PDF para la reunión", id);
        }
        return reunion;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void validateStatusTransition(EstadoReunion current, EstadoReunion next) {
        boolean valid =
                (current == EstadoReunion.NO_INICIADA && next == EstadoReunion.EN_CURSO) ||
                (current == EstadoReunion.EN_CURSO     && next == EstadoReunion.BLOQUEADA) ||
                (current == EstadoReunion.BLOQUEADA    && next == EstadoReunion.EN_CURSO);

        if (!valid) {
            throw new BusinessException(String.format(
                    "Transición inválida: %s → %s. Permitidas: NO_INICIADA→EN_CURSO, EN_CURSO↔BLOQUEADA.",
                    current, next));
        }
    }

    private void validateNotBlocked(Reunion reunion) {
        if (reunion.getEstado() == EstadoReunion.BLOQUEADA) {
            throw new BusinessException(
                    "No se puede modificar la reunión '" + reunion.getTitulo() + "' porque está BLOQUEADA.");
        }
    }

    private Reunion findOrThrow(Long id) {
        return reunionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", id));
    }

    private Usuario findUsuarioOrThrow(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private MeetingResponse mapToResponse(Reunion r) {
        String accessCode = (r.getEstado() == EstadoReunion.EN_CURSO) ? r.getAccessCode() : null;
        return MeetingResponse.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .descripcion(r.getDescripcion())
                .fechaInicio(r.getFechaInicio())
                .estado(r.getEstado())
                .accessCode(accessCode)
                .recordingLink(r.getRecordingLink())
                .presentacionLink(r.getPresentacionLink())
                .newsLink(r.getNewsLink())
                .activityLink(r.getActivityLink())
                .linksExtra(deserializeLinks(r.getLinksExtraJson()))
                .newsLinksExtra(deserializeLinks(r.getNewsLinksExtraJson()))
                .pdfFileName(r.getPdfFileName())
                .hasPdfFile(r.getPdfFileName() != null && !r.getPdfFileName().isBlank())
                .creadoPorUsername(r.getCreadoPor().getUsername())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @SuppressWarnings("unchecked")
    private List<String> deserializeLinks(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return new ArrayList<>();
        try { return MAPPER.readValue(json, List.class); } catch (Exception e) { return new ArrayList<>(); }
    }

    private String serializeLinks(List<String> links) {
        if (links == null || links.isEmpty()) return "[]";
        try { return MAPPER.writeValueAsString(links); } catch (Exception e) { return "[]"; }
    }
}
