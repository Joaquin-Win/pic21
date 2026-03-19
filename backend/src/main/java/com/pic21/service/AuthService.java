package com.pic21.service;

import com.pic21.domain.Role;
import com.pic21.domain.User;
import com.pic21.dto.request.LoginRequest;
import com.pic21.dto.request.RegisterRequest;
import com.pic21.dto.response.AuthResponse;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.RoleRepository;
import com.pic21.repository.UserRepository;
import com.pic21.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación: login y registro de usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ---------------------------------------------------------
    // Login
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", 0L));

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        log.info("Login exitoso para usuario: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }

    // ---------------------------------------------------------
    // Registro (solo ADMIN)
    // ---------------------------------------------------------

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Validar unicidad de username y email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El nombre de usuario '" + request.getUsername() + "' ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email '" + request.getEmail() + "' ya está registrado");
        }

        // Rol por defecto: ESTUDIANTE
        Role.RoleName roleName = (request.getRole() != null)
                ? request.getRole()
                : Role.RoleName.ESTUDIANTE;

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        log.info("Usuario creado: {} con rol {}", user.getUsername(), roleName);

        return mapToUserResponse(user);
    }

    // ---------------------------------------------------------
    // Helper
    // ---------------------------------------------------------

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toList()))
                .build();
    }
}
