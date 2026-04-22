package com.pic21.service;

import com.pic21.domain.*;
import com.pic21.dto.request.LoginRequest;
import com.pic21.dto.request.RegisterRequest;
import com.pic21.dto.response.AuthResponse;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.UsuarioRepository;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación: login y registro de usuarios (UML v8).
 *
 * Grupo A (PerfilPersonal): R01_PROFESOR, R03_EGRESADO, R04_ADMIN, R05_DIRECTOR
 * Grupo B (PerfilEstudiantil): R02_ESTUDIANTE, R06_AYUDANTE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ── Login ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalized = request.getUsername().toLowerCase().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalized, request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(normalized)
                .or(() -> usuarioRepository.findByCredencial_EmailIgnoreCase(normalized))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", 0L));

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::name)
                .collect(Collectors.toList());

        log.info("Login exitoso: {}", normalized);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getCredencial().getEmail())
                .roles(roles)
                .build();
    }

    // ── Registro (solo ADMIN) ─────────────────────────────────────

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail    = request.getEmail().toLowerCase().trim();
        String normalizedUsername = request.getUsername().toLowerCase().trim();

        // Validar unicidad
        if (usuarioRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new BusinessException("El username '" + normalizedUsername + "' ya está en uso.");
        }
        if (usuarioRepository.existsByCredencial_EmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("El email '" + normalizedEmail + "' ya está registrado.");
        }

        // Rol por defecto: R02_ESTUDIANTE
        Rol rol = request.getRol() != null ? request.getRol() : Rol.R02_ESTUDIANTE;
        Set<Rol> roles = EnumSet.of(rol);

        // Validar contraseña fuerte
        validatePassword(request.getPassword());

        // Construir Credencial
        Credencial credencial = Credencial.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        // Construir perfiles según grupo
        boolean grupoA = isGrupoA(rol);
        boolean grupoB = isGrupoB(rol);

        PerfilPersonal perfilPersonal = null;
        if (grupoA) {
            String dni = request.getDni() != null ? request.getDni().trim() : "";
            if (dni.isEmpty() || !dni.matches("^\\d{8}$")) {
                throw new BusinessException("DNI obligatorio (8 dígitos) para Grupo A.");
            }
            perfilPersonal = PerfilPersonal.builder()
                    .dni(dni)
                    .correo(request.getCorreo())
                    .build();
        }

        PerfilEstudiantil perfilEstudiantil = null;
        if (grupoB) {
            perfilEstudiantil = PerfilEstudiantil.builder()
                    .correoInstitucional(request.getCorreoInstitucional())
                    .legajo(request.getLegajo())
                    .carrera(request.getCarrera())
                    .build();
        }

        Usuario usuario = Usuario.builder()
                .username(normalizedUsername)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .roles(roles)
                .activo(true)
                .credencial(credencial)
                .perfilPersonal(perfilPersonal)
                .perfilEstudiantil(perfilEstudiantil)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario creado: {} con rol {}", normalizedUsername, rol);

        return mapToUserResponse(usuario);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void validatePassword(String pwd) {
        if (!pwd.matches(".*[A-Z].*")) {
            throw new BusinessException("La contraseña debe incluir al menos 1 mayúscula.");
        }
        if (!pwd.matches(".*[0-9].*")) {
            throw new BusinessException("La contraseña debe incluir al menos 1 número.");
        }
        if (!pwd.matches(".*[@#$%^&+=!_.\\-].*")) {
            throw new BusinessException("La contraseña debe incluir al menos 1 símbolo (@#$!. etc).");
        }
    }

    private boolean isGrupoA(Rol rol) {
        return rol == Rol.R01_PROFESOR || rol == Rol.R03_EGRESADO
                || rol == Rol.R04_ADMIN || rol == Rol.R05_DIRECTOR;
    }

    private boolean isGrupoB(Rol rol) {
        return rol == Rol.R02_ESTUDIANTE || rol == Rol.R06_AYUDANTE;
    }

    public UserResponse mapToUserResponse(Usuario u) {
        String email = u.getCredencial() != null ? u.getCredencial().getEmail() : null;
        String dni = null, correo = null;
        if (u.getPerfilPersonal() != null) {
            dni    = u.getPerfilPersonal().getDni();
            correo = u.getPerfilPersonal().getCorreo();
        }
        String correoInstitucional = null, legajo = null, carrera = null;
        if (u.getPerfilEstudiantil() != null) {
            correoInstitucional = u.getPerfilEstudiantil().getCorreoInstitucional();
            legajo  = u.getPerfilEstudiantil().getLegajo();
            carrera = u.getPerfilEstudiantil().getCarrera();
        }

        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .activo(u.isActivo())
                .fechaRegistro(u.getFechaRegistro())
                .email(email)
                .dni(dni)
                .correo(correo)
                .correoInstitucional(correoInstitucional)
                .legajo(legajo)
                .carrera(carrera)
                .roles(u.getRoles().stream().map(Rol::name).sorted().collect(Collectors.toList()))
                .build();
    }
}
