package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.dto.request.AttendanceRequest;
import com.pic21.dto.response.AttendanceResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de asistencias (UML v8).
 *
 * Reglas:
 *   1. Solo se puede registrar asistencia si la reunión está EN_CURSO.
 *   2. Un usuario no puede registrar más de una asistencia por reunión.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AsistenciaRepository asistenciaRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public AttendanceResponse registerSelf(Long reunionId, String username, AttendanceRequest request) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", reunionId));

        if (reunion.getEstado() != EstadoReunion.EN_CURSO) {
            throw new BusinessException(
                    "Solo se puede registrar asistencia cuando la reunión está EN_CURSO. " +
                    "Estado actual: " + reunion.getEstado());
        }

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        if (asistenciaRepository.existsByReunionAndUsuario(reunion, usuario)) {
            throw new BusinessException(
                    "Ya registraste tu asistencia en '" + reunion.getTitulo() + "'. No podés registrarte dos veces.");
        }

        Asistencia asistencia = Asistencia.builder()
                .reunion(reunion)
                .usuario(usuario)
                .presente(request != null && request.isPresente())
                .build();

        Asistencia saved = asistenciaRepository.save(asistencia);
        log.info("Asistencia registrada: user='{}' reunion='{}' presente={}",
                username, reunion.getTitulo(), saved.isPresente());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByReunion(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", reunionId));

        return asistenciaRepository.findByReunionWithDetails(reunion)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AttendanceResponse mapToResponse(Asistencia a) {
        String email = a.getUsuario().getCredencial() != null
                ? a.getUsuario().getCredencial().getEmail() : null;

        return AttendanceResponse.builder()
                .id(a.getId())
                .reunionId(a.getReunion().getId())
                .reunionTitulo(a.getReunion().getTitulo())
                .usuarioId(a.getUsuario().getId())
                .username(a.getUsuario().getUsername())
                .nombre(a.getUsuario().getNombre())
                .apellido(a.getUsuario().getApellido())
                .email(email)
                .presente(a.isPresente())
                .fechaRegistro(a.getFechaRegistro())
                .build();
    }
}
