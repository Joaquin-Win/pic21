/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Asistencia
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.request.AttendanceRequest
 *  com.pic21.dto.response.AttendanceResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.AsistenciaRepository
 *  com.pic21.repository.ReunionRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.AttendanceService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.pic21.domain.Asistencia;
import com.pic21.domain.EstadoReunion;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import com.pic21.dto.request.AttendanceRequest;
import com.pic21.dto.response.AttendanceResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final AsistenciaRepository asistenciaRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public AttendanceResponse registerSelf(Long reunionId, String username, AttendanceRequest request) {
        Reunion reunion = (Reunion)this.reunionRepository.findById(reunionId).orElseThrow(() -> new ResourceNotFoundException("Reuni\u00f3n", reunionId));
        if (reunion.getEstado() != EstadoReunion.EN_CURSO) {
            throw new BusinessException("Solo se puede registrar asistencia cuando la reuni\u00f3n est\u00e1 EN_CURSO. Estado actual: " + String.valueOf(reunion.getEstado()));
        }
        Usuario usuario = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        if (this.asistenciaRepository.existsByReunionAndUsuario(reunion, usuario)) {
            throw new BusinessException("Ya registraste tu asistencia en '" + reunion.getTitulo() + "'. No pod\u00e9s registrarte dos veces.");
        }
        Asistencia asistencia = Asistencia.builder().reunion(reunion).usuario(usuario).presente(request != null && request.isPresente()).build();
        Asistencia saved = (Asistencia)this.asistenciaRepository.save(asistencia);
        log.info("Asistencia registrada: user='{}' reunion='{}' presente={}", new Object[]{username, reunion.getTitulo(), saved.isPresente()});
        return this.mapToResponse(saved);
    }

    @Transactional(readOnly=true)
    public List<AttendanceResponse> findByReunion(Long reunionId) {
        Reunion reunion = (Reunion)this.reunionRepository.findById(reunionId).orElseThrow(() -> new ResourceNotFoundException("Reuni\u00f3n", reunionId));
        return this.asistenciaRepository.findByReunionWithDetails(reunion).stream().map(arg_0 -> this.mapToResponse(arg_0)).collect(Collectors.toList());
    }

    private AttendanceResponse mapToResponse(Asistencia a) {
        String email = a.getUsuario().getCredencial() != null ? a.getUsuario().getCredencial().getEmail() : null;
        return AttendanceResponse.builder().id(a.getId()).reunionId(a.getReunion().getId()).reunionTitulo(a.getReunion().getTitulo()).usuarioId(a.getUsuario().getId()).username(a.getUsuario().getUsername()).nombre(a.getUsuario().getNombre()).apellido(a.getUsuario().getApellido()).email(email).presente(a.isPresente()).fechaRegistro(a.getFechaRegistro()).build();
    }

    public AttendanceService(AsistenciaRepository asistenciaRepository, ReunionRepository reunionRepository, UsuarioRepository usuarioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.reunionRepository = reunionRepository;
        this.usuarioRepository = usuarioRepository;
    }
}

