/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.PerfilEstudiantil
 *  com.pic21.domain.PerfilPersonal
 *  com.pic21.domain.Rol
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.request.UpdateUserRequest
 *  com.pic21.dto.response.UserResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.AsignacionTareaRepository
 *  com.pic21.repository.AsistenciaRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.AuthService
 *  com.pic21.service.UserService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.dao.DataIntegrityViolationException
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.pic21.domain.PerfilEstudiantil;
import com.pic21.domain.PerfilPersonal;
import com.pic21.domain.Rol;
import com.pic21.domain.Usuario;
import com.pic21.dto.request.UpdateUserRequest;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AsignacionTareaRepository;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.UsuarioRepository;
import com.pic21.service.AuthService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UsuarioRepository usuarioRepository;
    private final AsignacionTareaRepository asignacionTareaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final AuthService authService;

    @Transactional(readOnly=true)
    public List<UserResponse> findAll() {
        return this.usuarioRepository.findAll().stream()
            .sorted((a, b) -> {
                int c = a.getNombre() != null && b.getNombre() != null
                    ? a.getNombre().compareToIgnoreCase(b.getNombre()) : 0;
                if (c != 0) return c;
                return a.getApellido() != null && b.getApellido() != null
                    ? a.getApellido().compareToIgnoreCase(b.getApellido()) : 0;
            })
            .map(u -> this.authService.mapToUserResponse(u))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public UserResponse findById(Long id) {
        return this.authService.mapToUserResponse(this.findOrThrow(id));
    }

    @Transactional
    public UserResponse updateRoles(Long id, List<String> roleNames, String adminUsername) {
        Usuario usuario = this.findOrThrow(id);
        if (usuario.getUsername().equals(adminUsername)) {
            throw new BusinessException("No pod\u00e9s modificar tus propios roles.");
        }
        Set newRoles = roleNames.stream().map(rn -> {
            try {
                return Rol.valueOf((String)rn.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                throw new BusinessException("Rol inv\u00e1lido: " + rn + ". V\u00e1lidos: R01_PROFESOR, R02_ESTUDIANTE, R03_EGRESADO, R04_ADMIN, R05_DIRECTOR, R06_AYUDANTE, R07_ESTUDIANTE_POSGRADO");
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(Rol.class)));
        if (newRoles.isEmpty()) {
            throw new BusinessException("El usuario debe tener al menos un rol.");
        }
        usuario.setRoles(newRoles);
        log.info("Roles actualizados para '{}': {}", usuario.getUsername(), roleNames);
        return this.authService.mapToUserResponse((Usuario)this.usuarioRepository.save(usuario));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateUserRequest request, String adminUsername) {
        Usuario usuario = this.findOrThrow(id);
        if (!usuario.getUsername().equalsIgnoreCase(request.getUsername())) {
            this.usuarioRepository.findByUsernameIgnoreCase(request.getUsername()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("El username ya existe.");
                }
            });
        }
        usuario.setUsername(request.getUsername());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        if (usuario.esGrupoA()) {
            PerfilPersonal pp = usuario.getPerfilPersonal() != null ? usuario.getPerfilPersonal() : new PerfilPersonal();
            pp.setDni(request.getDni());
            pp.setCorreo(request.getCorreo());
            usuario.setPerfilPersonal(pp);
        }
        if (usuario.esGrupoB()) {
            PerfilEstudiantil pe = usuario.getPerfilEstudiantil() != null ? usuario.getPerfilEstudiantil() : new PerfilEstudiantil();
            pe.setCorreoInstitucional(request.getCorreoInstitucional());
            pe.setLegajo(request.getLegajo());
            pe.setCarrera(request.getCarrera());
            usuario.setPerfilEstudiantil(pe);
        }
        log.info("Perfil id={} actualizado por '{}'", id, adminUsername);
        return this.authService.mapToUserResponse((Usuario)this.usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(Long id, String adminUsername) {
        Usuario usuario = this.findOrThrow(id);
        if (usuario.getUsername().equals(adminUsername)) {
            throw new BusinessException("No pod\u00e9s eliminar tu propia cuenta.");
        }
        try {
            this.asignacionTareaRepository.deleteByUsuarioId(id);
            this.asistenciaRepository.deleteByUsuarioId(id);
            this.usuarioRepository.flush();
            this.usuarioRepository.delete(usuario);
            this.usuarioRepository.flush();
            log.info("Usuario '{}' (id={}) eliminado por '{}'", new Object[]{usuario.getUsername(), id, adminUsername});
        }
        catch (DataIntegrityViolationException ex) {
            log.error("FK error al eliminar usuario id={}: {}", id, ex.getMessage());
            throw new BusinessException("No se puede eliminar: tiene datos asociados (reuniones, tareas creadas). Deshabilit\u00e1 en su lugar.");
        }
    }

    @Transactional
    public UserResponse toggleActivo(Long id, String adminUsername) {
        Usuario usuario = this.findOrThrow(id);
        if (usuario.getUsername().equals(adminUsername)) {
            throw new BusinessException("No pod\u00e9s deshabilitar tu propia cuenta.");
        }
        usuario.setActivo(!usuario.isActivo());
        log.info("Usuario '{}' {} por '{}'", new Object[]{usuario.getUsername(), usuario.isActivo() ? "activado" : "desactivado", adminUsername});
        return this.authService.mapToUserResponse((Usuario)this.usuarioRepository.save(usuario));
    }

    private Usuario findOrThrow(Long id) {
        return (Usuario)this.usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    public UserService(UsuarioRepository usuarioRepository, AsignacionTareaRepository asignacionTareaRepository, AsistenciaRepository asistenciaRepository, AuthService authService) {
        this.usuarioRepository = usuarioRepository;
        this.asignacionTareaRepository = asignacionTareaRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.authService = authService;
    }
}

