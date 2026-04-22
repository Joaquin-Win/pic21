package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.dto.request.UpdateUserRequest;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios (UML v8).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final AsignacionTareaRepository asignacionTareaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(authService::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return authService.mapToUserResponse(findOrThrow(id));
    }

    @Transactional
    public UserResponse updateRoles(Long id, List<String> roleNames, String adminUsername) {
        Usuario usuario = findOrThrow(id);

        if (usuario.getUsername().equals(adminUsername)) {
            throw new BusinessException("No podés modificar tus propios roles.");
        }

        Set<Rol> newRoles = roleNames.stream()
                .map(rn -> {
                    try {
                        return Rol.valueOf(rn.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new BusinessException("Rol inválido: " + rn +
                                ". Válidos: R01_PROFESOR, R02_ESTUDIANTE, R03_EGRESADO, R04_ADMIN, R05_DIRECTOR, R06_AYUDANTE");
                    }
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Rol.class)));

        if (newRoles.isEmpty()) {
            throw new BusinessException("El usuario debe tener al menos un rol.");
        }

        usuario.setRoles(newRoles);
        log.info("Roles actualizados para '{}': {}", usuario.getUsername(), roleNames);
        return authService.mapToUserResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateUserRequest request, String adminUsername) {
        Usuario usuario = findOrThrow(id);

        // Validar unicidad de username si cambió
        if (!usuario.getUsername().equalsIgnoreCase(request.getUsername())) {
            usuarioRepository.findByUsernameIgnoreCase(request.getUsername()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("El username ya existe.");
                }
            });
        }

        usuario.setUsername(request.getUsername());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());

        // Actualizar PerfilPersonal si aplica
        if (usuario.esGrupoA()) {
            PerfilPersonal pp = usuario.getPerfilPersonal() != null
                    ? usuario.getPerfilPersonal()
                    : new PerfilPersonal();
            pp.setDni(request.getDni());
            pp.setCorreo(request.getCorreo());
            usuario.setPerfilPersonal(pp);
        }

        // Actualizar PerfilEstudiantil si aplica
        if (usuario.esGrupoB()) {
            PerfilEstudiantil pe = usuario.getPerfilEstudiantil() != null
                    ? usuario.getPerfilEstudiantil()
                    : new PerfilEstudiantil();
            pe.setCorreoInstitucional(request.getCorreoInstitucional());
            pe.setLegajo(request.getLegajo());
            pe.setCarrera(request.getCarrera());
            usuario.setPerfilEstudiantil(pe);
        }

        log.info("Perfil id={} actualizado por '{}'", id, adminUsername);
        return authService.mapToUserResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(Long id, String adminUsername) {
        Usuario usuario = findOrThrow(id);

        if (usuario.getUsername().equals(adminUsername)) {
            throw new BusinessException("No podés eliminar tu propia cuenta.");
        }

        try {
            asignacionTareaRepository.deleteByUsuarioId(id);
            asistenciaRepository.deleteByUsuarioId(id);
            usuarioRepository.flush();
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
            log.info("Usuario '{}' (id={}) eliminado por '{}'", usuario.getUsername(), id, adminUsername);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            log.error("FK error al eliminar usuario id={}: {}", id, ex.getMessage());
            throw new BusinessException(
                    "No se puede eliminar: tiene datos asociados (reuniones, tareas creadas). Deshabilitá en su lugar.");
        }
    }

    @Transactional
    public UserResponse toggleActivo(Long id, String adminUsername) {
        Usuario usuario = findOrThrow(id);

        if (usuario.getUsername().equals(adminUsername)) {
            throw new BusinessException("No podés deshabilitar tu propia cuenta.");
        }

        usuario.setActivo(!usuario.isActivo());
        log.info("Usuario '{}' {} por '{}'",
                usuario.getUsername(), usuario.isActivo() ? "activado" : "desactivado", adminUsername);
        return authService.mapToUserResponse(usuarioRepository.save(usuario));
    }

    private Usuario findOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
