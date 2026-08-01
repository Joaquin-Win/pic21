/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Usuario
 *  com.pic21.repository.UsuarioRepository
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.stereotype.Repository
 */
package com.pic21.repository;

import com.pic21.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository
extends JpaRepository<Usuario, Long> {
    public Optional<Usuario> findByUsernameIgnoreCase(String var1);

    public Optional<Usuario> findByCredencial_EmailIgnoreCase(String var1);

    public boolean existsByUsernameIgnoreCase(String var1);

    public boolean existsByCredencial_EmailIgnoreCase(String var1);
}

