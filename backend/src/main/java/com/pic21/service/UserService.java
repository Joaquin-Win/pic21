package com.pic21.service;

import com.pic21.domain.Role;
import com.pic21.domain.User;
import com.pic21.dto.request.UpdateUserRequest;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.RoleRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios (solo accesible para ADMIN).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // -----------------------------------------------------------------------
    // Listar todos los usuarios
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Obtener un usuario por ID
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return mapToResponse(findUserOrThrow(id));
    }

    // -----------------------------------------------------------------------
    // Actualizar roles de un usuario
    // -----------------------------------------------------------------------

    @Transactional
    public UserResponse updateRoles(Long id, List<String> roleNames, String adminUsername) {
        User user = findUserOrThrow(id);

        if (user.getUsername().equals(adminUsername)) {
            throw new BusinessException("No podés modificar tus propios roles.");
        }

        Set<Role> newRoles = roleNames.stream()
                .map(rn -> {
                    try {
                        Role.RoleName roleName = Role.RoleName.valueOf(rn.toUpperCase());
                        return roleRepository.findByName(roleName)
                                .orElseThrow(() -> new BusinessException("Rol inválido: " + rn));
                    } catch (IllegalArgumentException e) {
                        throw new BusinessException("Rol inválido: " + rn + ". Roles válidos: ADMIN, PROFESOR, AYUDANTE, ESTUDIANTE.");
                    }
                })
                .collect(Collectors.toSet());

        if (newRoles.isEmpty()) {
            throw new BusinessException("El usuario debe tener al menos un rol.");
        }

        user.setRoles(newRoles);
        log.info("Roles actualizados para usuario '{}': {}", user.getUsername(), roleNames);
        return mapToResponse(userRepository.save(user));
    }

    // -----------------------------------------------------------------------
    // Editar perfil (nombre, apellido, email, username)
    // -----------------------------------------------------------------------

    @Transactional
    public UserResponse updateProfile(Long id, UpdateUserRequest request, String adminUsername) {
        User user = findUserOrThrow(id);

        // Validar unicidad de email si cambió
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("El email ya está en uso por otro usuario.");
                }
            });
        }

        // Validar unicidad de username si cambió
        if (!user.getUsername().equalsIgnoreCase(request.getUsername())) {
            userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("El nombre de usuario ya existe.");
                }
            });
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());

        log.info("Perfil del usuario id={} actualizado por '{}'", id, adminUsername);
        return mapToResponse(userRepository.save(user));
    }

    // -----------------------------------------------------------------------
    // Eliminar usuario
    // -----------------------------------------------------------------------

    @Transactional
    public void delete(Long id, String adminUsername) {
        User user = findUserOrThrow(id);

        if (user.getUsername().equals(adminUsername)) {
            throw new BusinessException("No podés eliminar tu propia cuenta.");
        }

        userRepository.delete(user);
        log.info("Usuario '{}' eliminado por '{}'", user.getUsername(), adminUsername);
    }

    // -----------------------------------------------------------------------
    // Habilitar / deshabilitar usuario
    // -----------------------------------------------------------------------

    @Transactional
    public UserResponse toggleEnabled(Long id, String adminUsername) {
        User user = findUserOrThrow(id);

        if (user.getUsername().equals(adminUsername)) {
            throw new BusinessException("No podés deshabilitar tu propia cuenta.");
        }

        user.setEnabled(!user.isEnabled());
        log.info("Usuario '{}' {} por '{}'",
                user.getUsername(), user.isEnabled() ? "habilitado" : "deshabilitado", adminUsername);
        return mapToResponse(userRepository.save(user));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private UserResponse mapToResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .roles(u.getRoles().stream()
                        .map(r -> r.getName().name())
                        .sorted()
                        .collect(Collectors.toList()))
                .build();
    }
}
