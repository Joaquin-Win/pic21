/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.request.MeetingRequest
 *  com.pic21.dto.response.MeetingResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.ReunionRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.MeetingService
 *  jakarta.persistence.EntityManager
 *  jakarta.persistence.PersistenceContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.util.StringUtils
 *  org.springframework.web.multipart.MultipartFile
 */
package com.pic21.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pic21.domain.EstadoReunion;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import com.pic21.dto.request.MeetingRequest;
import com.pic21.dto.response.MeetingResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingService {
    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Transactional
    public MeetingResponse create(MeetingRequest request, String username) {
        Usuario creator = this.findUsuarioOrThrow(username);
        Reunion reunion = Reunion.builder().titulo(request.getTitle()).descripcion(request.getDescription()).fechaInicio(request.getScheduledAt()).estado(EstadoReunion.NO_INICIADA).accessCode(request.getAccessCode()).recordingLink(request.getRecordingLink()).newsLink(request.getNewsLink()).activityLink(request.getActivityLink()).presentacionLink(request.getPresentacionLink()).linksExtraJson(this.serializeLinks(request.getLinksExtra())).newsLinksExtraJson(this.serializeLinks(request.getNewsLinksExtra())).creadoPor(creator).build();
        Reunion saved = (Reunion)this.reunionRepository.save(reunion);
        log.info("Reuni\u00f3n creada: id={}, t\u00edtulo='{}', por='{}'", new Object[]{saved.getId(), saved.getTitulo(), username});
        return this.mapToResponse(saved);
    }

    @Transactional
    public MeetingResponse update(Long id, MeetingRequest request, boolean isAdmin) {
        Reunion reunion = this.findOrThrow(id);
        if (!isAdmin) {
            this.validateNotBlocked(reunion);
        }
        reunion.setTitulo(request.getTitle());
        reunion.setDescripcion(request.getDescription());
        reunion.setFechaInicio(request.getScheduledAt());
        reunion.setAccessCode(request.getAccessCode());
        reunion.setRecordingLink(request.getRecordingLink());
        reunion.setNewsLink(request.getNewsLink());
        reunion.setActivityLink(request.getActivityLink());
        reunion.setPresentacionLink(request.getPresentacionLink());
        reunion.setLinksExtraJson(this.serializeLinks(request.getLinksExtra()));
        reunion.setNewsLinksExtraJson(this.serializeLinks(request.getNewsLinksExtra()));
        log.info("Reuni\u00f3n actualizada: id={}{}", id, (isAdmin ? " (por ADMIN, reuni\u00f3n bloqueada)" : ""));
        return this.mapToResponse((Reunion)this.reunionRepository.save(reunion));
    }

    @Transactional
    public MeetingResponse update(Long id, MeetingRequest request) {
        return this.update(id, request, false);
    }

    @Transactional
    public void delete(Long id) {
        if (!this.reunionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reuni\u00f3n", id);
        }
        this.entityManager.createNativeQuery("DELETE FROM asistencias WHERE reunion_id = :id").setParameter("id", id).executeUpdate();
        this.entityManager.createNativeQuery("DELETE FROM asignaciones_tarea WHERE tarea_id IN (SELECT id FROM tareas WHERE reunion_id = :id)").setParameter("id", id).executeUpdate();
        this.entityManager.createNativeQuery("DELETE FROM tareas WHERE reunion_id = :id").setParameter("id", id).executeUpdate();
        this.entityManager.createNativeQuery("DELETE FROM meeting_files WHERE meeting_id = :id").setParameter("id", id).executeUpdate();
        this.entityManager.createNativeQuery("DELETE FROM reuniones WHERE id = :id").setParameter("id", id).executeUpdate();
        log.info("Reuni\u00f3n id={} eliminada con asistencias, tareas y archivos", id);
    }

    @Transactional
    public MeetingResponse changeStatus(Long id, EstadoReunion newEstado, boolean isAdmin) {
        Reunion reunion = this.findOrThrow(id);
        if (reunion.getEstado() == EstadoReunion.BLOQUEADA && newEstado == EstadoReunion.EN_CURSO && !isAdmin) {
            throw new BusinessException("Solo ADMIN puede desbloquear reuniones.");
        }
        this.validateStatusTransition(reunion.getEstado(), newEstado);
        EstadoReunion previous = reunion.getEstado();
        reunion.setEstado(newEstado);
        log.info("Reuni\u00f3n id={} cambi\u00f3 estado: {} \u2192 {}", new Object[]{id, previous, newEstado});
        return this.mapToResponse((Reunion)this.reunionRepository.save(reunion));
    }

    @Transactional
    public MeetingResponse changeStatus(Long id, EstadoReunion newEstado) {
        return this.changeStatus(id, newEstado, false);
    }

    @Transactional(readOnly=true)
    public Page<MeetingResponse> findAll(Pageable pageable) {
        return this.reunionRepository.findAll(pageable).map(arg_0 -> this.mapToResponse(arg_0));
    }

    @Transactional(readOnly=true)
    public MeetingResponse findById(Long id) {
        return this.mapToResponse(this.findOrThrow(id));
    }

    @Transactional
    public MeetingResponse uploadPdf(Long id, MultipartFile file) {
        Reunion reunion = this.findOrThrow(id);
        this.validateNotBlocked(reunion);
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                throw new BusinessException("El archivo debe ser un PDF v\u00e1lido.");
            }
            reunion.setPdfFileData(file.getBytes());
            reunion.setPdfFileName(StringUtils.cleanPath((String)(file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento.pdf")));
        }
        catch (IOException e) {
            throw new BusinessException("Error al procesar el archivo PDF: " + e.getMessage());
        }
        log.info("PDF subido para reuni\u00f3n id={}", id);
        return this.mapToResponse((Reunion)this.reunionRepository.save(reunion));
    }

    @Transactional(readOnly=true)
    public Reunion getReunionWithPdf(Long id) {
        Reunion reunion = this.findOrThrow(id);
        if (reunion.getPdfFileData() == null) {
            throw new ResourceNotFoundException("Archivo PDF para la reuni\u00f3n", id);
        }
        return reunion;
    }

    private void validateStatusTransition(EstadoReunion current, EstadoReunion next) {
        boolean valid;
        boolean bl = valid = current == EstadoReunion.NO_INICIADA && next == EstadoReunion.EN_CURSO || current == EstadoReunion.EN_CURSO && next == EstadoReunion.BLOQUEADA || current == EstadoReunion.BLOQUEADA && next == EstadoReunion.EN_CURSO;
        if (!valid) {
            throw new BusinessException(String.format("Transici\u00f3n inv\u00e1lida: %s \u2192 %s. Permitidas: NO_INICIADA\u2192EN_CURSO, EN_CURSO\u2194BLOQUEADA.", current, next));
        }
    }

    private void validateNotBlocked(Reunion reunion) {
        if (reunion.getEstado() == EstadoReunion.BLOQUEADA) {
            throw new BusinessException("No se puede modificar la reuni\u00f3n '" + reunion.getTitulo() + "' porque est\u00e1 BLOQUEADA.");
        }
    }

    private Reunion findOrThrow(Long id) {
        return (Reunion)this.reunionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reuni\u00f3n", id));
    }

    private Usuario findUsuarioOrThrow(String username) {
        return (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private MeetingResponse mapToResponse(Reunion r) {
        String accessCode = r.getEstado() == EstadoReunion.EN_CURSO ? r.getAccessCode() : null;
        return MeetingResponse.builder().id(r.getId()).titulo(r.getTitulo()).descripcion(r.getDescripcion()).fechaInicio(r.getFechaInicio()).estado(r.getEstado()).accessCode(accessCode).recordingLink(r.getRecordingLink()).presentacionLink(r.getPresentacionLink()).newsLink(r.getNewsLink()).activityLink(r.getActivityLink()).linksExtra(this.deserializeLinks(r.getLinksExtraJson())).newsLinksExtra(this.deserializeLinks(r.getNewsLinksExtraJson())).pdfFileName(r.getPdfFileName()).hasPdfFile(r.getPdfFileName() != null && !r.getPdfFileName().isBlank()).creadoPorUsername(r.getCreadoPor().getUsername()).createdAt(r.getCreatedAt()).build();
    }

    private List<String> deserializeLinks(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return new ArrayList<String>();
        }
        try {
            return (List)MAPPER.readValue(json, List.class);
        }
        catch (Exception e) {
            return new ArrayList<String>();
        }
    }

    private String serializeLinks(List<String> links) {
        if (links == null || links.isEmpty()) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(links);
        }
        catch (Exception e) {
            return "[]";
        }
    }

    public MeetingService(ReunionRepository reunionRepository, UsuarioRepository usuarioRepository) {
        this.reunionRepository = reunionRepository;
        this.usuarioRepository = usuarioRepository;
    }
}

