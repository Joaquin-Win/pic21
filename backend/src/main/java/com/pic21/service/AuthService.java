/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Credencial
 *  com.pic21.domain.PerfilEstudiantil
 *  com.pic21.domain.PerfilPersonal
 *  com.pic21.domain.Rol
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.request.LoginRequest
 *  com.pic21.dto.request.RegisterRequest
 *  com.pic21.dto.response.AuthResponse
 *  com.pic21.dto.response.UserResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.security.JwtTokenProvider
 *  com.pic21.service.AuthService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.security.authentication.AuthenticationManager
 *  org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.pic21.domain.Credencial;
import com.pic21.domain.PerfilEstudiantil;
import com.pic21.domain.PerfilPersonal;
import com.pic21.domain.Rol;
import com.pic21.domain.Usuario;
import com.pic21.dto.request.LoginRequest;
import com.pic21.dto.request.RegisterRequest;
import com.pic21.dto.response.AuthResponse;
import com.pic21.dto.response.UserResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.UsuarioRepository;
import com.pic21.security.JwtTokenProvider;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly=true)
    public AuthResponse login(LoginRequest request) {
        String normalized = request.getUsername().toLowerCase().trim();
        Authentication authentication = this.authenticationManager.authenticate((Authentication)new UsernamePasswordAuthenticationToken((Object)normalized, (Object)request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = this.jwtTokenProvider.generateToken(authentication);
        Usuario usuario = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(normalized).or(() -> this.usuarioRepository.findByCredencial_EmailIgnoreCase(normalized)).orElseThrow(() -> new ResourceNotFoundException("Usuario", Long.valueOf(0L)));
        List roles = usuario.getRoles().stream().map(Enum::name).collect(Collectors.toList());
        log.info("Login exitoso: {}", (Object)normalized);
        return AuthResponse.builder().token(token).type("Bearer").id(usuario.getId()).username(usuario.getUsername()).nombre(usuario.getNombre()).apellido(usuario.getApellido()).email(usuario.getCredencial().getEmail()).roles(roles).build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        String normalizedUsername = request.getUsername().toLowerCase().trim();
        if (this.usuarioRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new BusinessException("El username '" + normalizedUsername + "' ya est\u00e1 en uso.");
        }
        if (this.usuarioRepository.existsByCredencial_EmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("El email '" + normalizedEmail + "' ya est\u00e1 registrado.");
        }
        Rol rol = request.getRol() != null ? request.getRol() : Rol.R02_ESTUDIANTE;
        EnumSet<Rol> roles = EnumSet.of(rol);
        this.validatePassword(request.getPassword());
        Credencial credencial = Credencial.builder().email(normalizedEmail).passwordHash(this.passwordEncoder.encode((CharSequence)request.getPassword())).build();
        boolean grupoA = this.isGrupoA(rol);
        boolean grupoB = this.isGrupoB(rol);
        PerfilPersonal perfilPersonal = null;
        if (grupoA) {
            String dni;
            String string = dni = request.getDni() != null ? request.getDni().trim() : "";
            if (dni.isEmpty() || !dni.matches("^\\d{8}$")) {
                throw new BusinessException("DNI obligatorio (8 d\u00edgitos) para Grupo A.");
            }
            perfilPersonal = PerfilPersonal.builder().dni(dni).correo(request.getCorreo()).build();
        }
        PerfilEstudiantil perfilEstudiantil = null;
        if (grupoB) {
            perfilEstudiantil = PerfilEstudiantil.builder().correoInstitucional(request.getCorreoInstitucional()).legajo(request.getLegajo()).carrera(request.getCarrera()).build();
        }
        Usuario usuario = Usuario.builder().username(normalizedUsername).nombre(request.getNombre()).apellido(request.getApellido()).roles(roles).activo(true).credencial(credencial).perfilPersonal(perfilPersonal).perfilEstudiantil(perfilEstudiantil).build();
        this.usuarioRepository.save((Object)usuario);
        log.info("Usuario creado: {} con rol {}", (Object)normalizedUsername, (Object)rol);
        return this.mapToUserResponse(usuario);
    }

    private void validatePassword(String pwd) {
        if (!pwd.matches(".*[A-Z].*")) {
            throw new BusinessException("La contrase\u00f1a debe incluir al menos 1 may\u00fascula.");
        }
        if (!pwd.matches(".*[0-9].*")) {
            throw new BusinessException("La contrase\u00f1a debe incluir al menos 1 n\u00famero.");
        }
        if (!pwd.matches(".*[@#$%^&+=!_.\\-].*")) {
            throw new BusinessException("La contrase\u00f1a debe incluir al menos 1 s\u00edmbolo (@#$!. etc).");
        }
    }

    private boolean isGrupoA(Rol rol) {
        return rol == Rol.R01_PROFESOR || rol == Rol.R03_EGRESADO || rol == Rol.R04_ADMIN
            || rol == Rol.R05_DIRECTOR || rol == Rol.R07_ESTUDIANTE_POSGRADO;
    }

    private boolean isGrupoB(Rol rol) {
        return rol == Rol.R02_ESTUDIANTE || rol == Rol.R06_AYUDANTE;
    }

    public UserResponse mapToUserResponse(Usuario u) {
        String email = u.getCredencial() != null ? u.getCredencial().getEmail() : null;
        String dni = null;
        String correo = null;
        if (u.getPerfilPersonal() != null) {
            dni = u.getPerfilPersonal().getDni();
            correo = u.getPerfilPersonal().getCorreo();
        }
        String correoInstitucional = null;
        String legajo = null;
        String carrera = null;
        if (u.getPerfilEstudiantil() != null) {
            correoInstitucional = u.getPerfilEstudiantil().getCorreoInstitucional();
            legajo = u.getPerfilEstudiantil().getLegajo();
            carrera = u.getPerfilEstudiantil().getCarrera();
        }
        return UserResponse.builder().id(u.getId()).username(u.getUsername()).nombre(u.getNombre()).apellido(u.getApellido()).activo(u.isActivo()).fechaRegistro(u.getFechaRegistro()).email(email).dni(dni).correo(correo).correoInstitucional(correoInstitucional).legajo(legajo).carrera(carrera).roles(u.getRoles().stream().map(Enum::name).sorted().collect(Collectors.toList())).build();
    }

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
}

