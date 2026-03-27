package com.pic21.service;

import com.pic21.domain.Attendance;
import com.pic21.domain.Meeting;
import com.pic21.domain.MeetingStatus;
import com.pic21.domain.User;
import com.pic21.dto.response.AttendanceResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AttendanceRepository;
import com.pic21.repository.MeetingRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de asistencias con las reglas de negocio del módulo.
 *
 * Reglas:
 *   1. Solo se puede registrar asistencia si la reunión está ACTIVA.
 *   2. Un usuario no puede registrar más de una asistencia por reunión.
 *   3. El registro "self" usa el usuario autenticado del JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    // -----------------------------------------------------------------------
    // Auto-registro — cualquier usuario autenticado
    // -----------------------------------------------------------------------

    /**
     * Registra la asistencia del usuario autenticado a una reunión.
     * Los datos del formulario (legajo, materia) se guardan con el registro.
     */
    @Transactional
    public AttendanceResponse registerSelf(Long meetingId, String username,
                                           com.pic21.dto.request.AttendanceRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));

        if (meeting.getStatus() != MeetingStatus.ACTIVA) {
            throw new BusinessException(
                    "Solo se puede registrar asistencia cuando la reunión está ACTIVA. " +
                    "Estado actual: " + meeting.getStatus());
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + username));

        if (attendanceRepository.existsByMeetingAndUser(meeting, user)) {
            throw new BusinessException(
                    "Ya registraste tu asistencia en la reunión '"
                    + meeting.getTitle() + "'. No podés registrarte dos veces.");
        }

        Attendance attendance = Attendance.builder()
                .meeting(meeting)
                .user(user)
                .legajo(request != null ? request.getLegajo() : null)
                .carrera(request != null ? request.getCarrera() : null)
                .tipoUsuario(request != null ? request.getTipoUsuario() : null)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Asistencia registrada: user='{}' meeting='{}' legajo='{}' carrera='{}'",
                username, meeting.getTitle(),
                saved.getLegajo(), saved.getCarrera());

        return mapToResponse(saved);
    }


    // -----------------------------------------------------------------------
    // Consulta por reunión — ADMIN / PROFESOR / AYUDANTE
    // (la seguridad se aplica en el Controller con @PreAuthorize)
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));

        return attendanceRepository.findByMeetingWithDetails(meeting)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .meetingId(a.getMeeting().getId())
                .meetingTitle(a.getMeeting().getTitle())
                .userId(a.getUser().getId())
                .username(a.getUser().getUsername())
                .firstName(a.getUser().getFirstName())
                .lastName(a.getUser().getLastName())
                .email(a.getUser().getEmail())
                .legajo(a.getLegajo())
                .carrera(a.getCarrera())
                .tipoUsuario(a.getTipoUsuario())
                .registeredAt(a.getRegisteredAt())
                .build();
    }
}
