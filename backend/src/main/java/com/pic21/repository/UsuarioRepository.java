package com.pic21.repository;

import com.pic21.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByCredencial_EmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByCredencial_EmailIgnoreCase(String email);
}
