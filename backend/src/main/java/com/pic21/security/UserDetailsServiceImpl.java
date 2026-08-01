/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Usuario
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.security.UserDetailsServiceImpl
 *  org.springframework.security.core.authority.SimpleGrantedAuthority
 *  org.springframework.security.core.userdetails.User
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.security.core.userdetails.UserDetailsService
 *  org.springframework.security.core.userdetails.UsernameNotFoundException
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.security;

import com.pic21.domain.Usuario;
import com.pic21.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl
implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username.toLowerCase().trim();
        Usuario usuario = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(normalized).or(() -> this.usuarioRepository.findByCredencial_EmailIgnoreCase(normalized)).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + normalized));
        List authorities = usuario.getRoles().stream().map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name())).collect(Collectors.toList());
        return User.builder().username(usuario.getUsername()).password(usuario.getCredencial().getPasswordHash()).authorities(authorities).disabled(!usuario.isActivo()).accountLocked(false).credentialsExpired(false).build();
    }

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
}

