package com.pic21.security;

import com.pic21.domain.Usuario;
import com.pic21.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de UserDetailsService para Spring Security (UML v8).
 * Carga el usuario desde la base de datos por username o email de Credencial.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username.toLowerCase().trim();

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(normalized)
                .or(() -> usuarioRepository.findByCredencial_EmailIgnoreCase(normalized))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + normalized));

        // Roles del enum Rol → GrantedAuthority con prefijo ROLE_
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getUsername())
                .password(usuario.getCredencial().getPasswordHash())
                .authorities(authorities)
                .disabled(!usuario.isActivo())
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
