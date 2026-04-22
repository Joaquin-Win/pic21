package com.pic21.service;

import com.pic21.domain.Meeting;
import com.pic21.domain.MeetingStatus;
import com.pic21.domain.User;
import com.pic21.dto.request.MeetingRequest;
import com.pic21.dto.response.MeetingResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.MeetingRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Servicio de reuniones con lógica de negocio y control de acceso por rol.
 *
 * Reglas de estado:
 *   NO_INICIADA → ACTIVA     : abrir registro de asistencia
 *   ACTIVA      → BLOQUEADA  : cerrar registro de asistencia
 *   BLOQUEADA   → ACTIVA     : solo ADMIN puede reactivar
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    // -----------------------------------------------------------------------
    // CREATE — ADMIN, PROFESOR
    // -----------------------------------------------------------------------

    @Transactional
    public MeetingResponse create(MeetingRequest request, String username) {
        User creator = findUserOrThrow(username);

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .scheduledAt(request.getScheduledAt())
                .status(MeetingStatus.NO_INICIADA)
                .accessCode(request.getAccessCode())
                .recordingLink(request.getRecordingLink())
                .newsLink(request.getNewsLink())
                .activityLink(request.getActivityLink())
                .createdBy(creator)
                .build();

        Meeting saved = meetingRepository.save(meeting);
        log.info("Reunión creada: id={}, título='{}', por='{}'",
                saved.getId(), saved.getTitle(), username);

        return mapToResponse(saved);
    }

    // -----------------------------------------------------------------------
    // UPDATE — ADMIN, PROFESOR; no se puede modificar si está BLOQUEADA
    // -----------------------------------------------------------------------

    @Transactional
    public MeetingResponse update(Long id, MeetingRequest request) {
        Meeting meeting = findMeetingOrThrow(id);
        validateNotBlocked(meeting);

        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setScheduledAt(request.getScheduledAt());
        meeting.setAccessCode(request.getAccessCode());
        meeting.setRecordingLink(request.getRecordingLink());
        meeting.setNewsLink(request.getNewsLink());
        meeting.setActivityLink(request.getActivityLink());

        log.info("Reunión actualizada: id={}", id);
        return mapToResponse(meetingRepository.save(meeting));
    }

    // -----------------------------------------------------------------------
    // DELETE — solo ADMIN (cascade via native SQL para evitar conflictos JPA)
    // -----------------------------------------------------------------------

    @Transactional
    public void delete(Long id) {
        if (!meetingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reunión", id);
        }
        // 1. Eliminar asistencias
        entityManager.createNativeQuery("DELETE FROM attendances WHERE meeting_id = :id")
                .setParameter("id", id).executeUpdate();
        // 2. Eliminar task_assignments ANTES que tasks (respeta FK task_assignments.task_id → tasks.id)
        entityManager.createNativeQuery(
                "DELETE FROM task_assignments WHERE task_id IN (SELECT id FROM tasks WHERE meeting_id = :id)")
                .setParameter("id", id).executeUpdate();
        // 3. Eliminar tasks
        entityManager.createNativeQuery("DELETE FROM tasks WHERE meeting_id = :id")
                .setParameter("id", id).executeUpdate();
        // 4. Eliminar archivos PDF asociados
        entityManager.createNativeQuery("DELETE FROM meeting_files WHERE meeting_id = :id")
                .setParameter("id", id).executeUpdate();
        // 5. Eliminar la reunión
        entityManager.createNativeQuery("DELETE FROM meetings WHERE id = :id")
                .setParameter("id", id).executeUpdate();
        log.info("Reunión id={} eliminada con attendances, task_assignments, tasks y files", id);
    }

    // -----------------------------------------------------------------------
    // CHANGE STATUS
    // -----------------------------------------------------------------------

    @Transactional
    public MeetingResponse changeStatus(Long id, MeetingStatus newStatus, boolean isAdmin) {
        Meeting meeting = findMeetingOrThrow(id);

        // Desbloquear (BLOQUEADA → ACTIVA): solo ADMIN
        if (meeting.getStatus() == MeetingStatus.BLOQUEADA
                && newStatus == MeetingStatus.ACTIVA
                && !isAdmin) {
            throw new BusinessException("Solo ADMIN puede desbloquear reuniones.");
        }

        validateStatusTransition(meeting.getStatus(), newStatus);

        MeetingStatus previous = meeting.getStatus();
        meeting.setStatus(newStatus);

        log.info("Reunión id={} cambió estado: {} → {}", id, previous, newStatus);
        return mapToResponse(meetingRepository.save(meeting));
    }

    /** Sobrecarga sin isAdmin — siempre deniega desbloqueo. */
    @Transactional
    public MeetingResponse changeStatus(Long id, MeetingStatus newStatus) {
        return changeStatus(id, newStatus, false);
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<MeetingResponse> findAll(Pageable pageable) {
        return meetingRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MeetingResponse findById(Long id) {
        return mapToResponse(findMeetingOrThrow(id));
    }

    // -----------------------------------------------------------------------
    // ARCHIVOS (PDF)
    // -----------------------------------------------------------------------

    @Transactional
    public MeetingResponse uploadPdf(Long id, MultipartFile file) {
        Meeting meeting = findMeetingOrThrow(id);
        validateNotBlocked(meeting);

        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                throw new BusinessException("El archivo debe ser un PDF válido.");
            }
            meeting.setPdfFileData(file.getBytes());
            meeting.setPdfFileName(StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento.pdf"));
        } catch (IOException e) {
            throw new BusinessException("Error al procesar el archivo PDF: " + e.getMessage());
        }

        log.info("PDF subido para reunión id={}", id);
        return mapToResponse(meetingRepository.save(meeting));
    }

    @Transactional(readOnly = true)
    public Meeting getMeetingWithPdf(Long id) {
        Meeting meeting = findMeetingOrThrow(id);
        if (meeting.getPdfFileData() == null) {
            throw new ResourceNotFoundException("Archivo PDF para la reunión", id);
        }
        return meeting;
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /**
     * Transiciones de estado permitidas:
     *   NO_INICIADA → ACTIVA    ✅
     *   ACTIVA      → BLOQUEADA ✅
     *   BLOQUEADA   → ACTIVA    ✅ (reactivar — solo ADMIN, verificado antes de llamar aquí)
     */
    private void validateStatusTransition(MeetingStatus current, MeetingStatus next) {
        boolean valid =
                (current == MeetingStatus.NO_INICIADA && next == MeetingStatus.ACTIVA) ||
                (current == MeetingStatus.ACTIVA      && next == MeetingStatus.BLOQUEADA) ||
                (current == MeetingStatus.BLOQUEADA   && next == MeetingStatus.ACTIVA);

        if (!valid) {
            throw new BusinessException(String.format(
                    "Transición de estado inválida: %s → %s. " +
                    "Permitidas: NO_INICIADA→ACTIVA, ACTIVA↔BLOQUEADA.",
                    current, next));
        }
    }

    private void validateNotBlocked(Meeting meeting) {
        if (meeting.getStatus() == MeetingStatus.BLOQUEADA) {
            throw new BusinessException(
                    "No se puede modificar la reunión '" + meeting.getTitle() +
                    "' porque está BLOQUEADA.");
        }
    }

    private Meeting findMeetingOrThrow(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", id));
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private MeetingResponse mapToResponse(Meeting m) {
        String accessCode = (m.getStatus() == MeetingStatus.ACTIVA) ? m.getAccessCode() : null;

        return MeetingResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .scheduledAt(m.getScheduledAt())
                .status(m.getStatus())
                .accessCode(accessCode)
                .recordingLink(m.getRecordingLink())
                .newsLink(m.getNewsLink())
                .activityLink(m.getActivityLink())
                .pdfFileName(m.getPdfFileName())
                .hasPdfFile(m.getPdfFileName() != null && !m.getPdfFileName().isBlank())
                .createdBy(m.getCreatedBy().getUsername())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
