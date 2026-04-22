package com.pic21.config;

import com.pic21.domain.*;
import com.pic21.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Inicializa datos al arrancar la aplicación (UML v8):
 * - Crea o actualiza el usuario administrador.
 * - Migra columnas necesarias en PostgreSQL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL    = "admin@pic21.com";
    private static final String ADMIN_PASSWORD  = "Msjj2023!";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        migrateColumns();
        initAdmin();
    }

    private void initAdmin() {
        Optional<Usuario> existing = usuarioRepository.findByUsernameIgnoreCase(ADMIN_USERNAME);
        if (existing.isPresent()) {
            Usuario admin = existing.get();
            boolean changed = false;

            if (!ADMIN_EMAIL.equals(admin.getCredencial().getEmail())) {
                admin.getCredencial().setEmail(ADMIN_EMAIL);
                changed = true;
            }
            if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getCredencial().getPasswordHash())) {
                admin.getCredencial().setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
                changed = true;
            }
            if (changed) {
                usuarioRepository.save(admin);
                log.info("Credenciales del admin actualizadas.");
            }
            return;
        }

        // Crear admin si no existe
        Credencial credencial = Credencial.builder()
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .build();

        PerfilPersonal perfil = PerfilPersonal.builder()
                .dni("00000000")
                .correo(ADMIN_EMAIL)
                .build();

        Usuario admin = Usuario.builder()
                .username(ADMIN_USERNAME)
                .nombre("Admin")
                .apellido("Admin")
                .roles(EnumSet.of(Rol.R04_ADMIN))
                .activo(true)
                .credencial(credencial)
                .perfilPersonal(perfil)
                .build();

        usuarioRepository.save(admin);
        log.info("Usuario admin creado (R04_ADMIN).");
    }

    /**
     * Migración de columnas para compatibilidad con PostgreSQL.
     * Usa IF NOT EXISTS / IF EXISTS para idempotencia.
     */
    private void migrateColumns() {
        try {
            // Asignaciones de tarea
            safeExecute("ALTER TABLE asignaciones_tarea ADD COLUMN IF NOT EXISTS score INTEGER");
            safeExecute("ALTER TABLE asignaciones_tarea ADD COLUMN IF NOT EXISTS attempts INTEGER DEFAULT 0");
            safeExecute("ALTER TABLE asignaciones_tarea ALTER COLUMN attempts SET DEFAULT 0");
            safeExecute("UPDATE asignaciones_tarea SET attempts = 0 WHERE attempts IS NULL");
            safeExecute("ALTER TABLE asignaciones_tarea ALTER COLUMN estado TYPE VARCHAR(20)");
            safeExecute("ALTER TABLE asignaciones_tarea DROP CONSTRAINT IF EXISTS asignaciones_tarea_estado_check");

            // Reuniones — links extra de noticias
            safeExecute("ALTER TABLE reuniones ADD COLUMN IF NOT EXISTS news_links_extra_json TEXT DEFAULT '[]'");

            // Tareas
            safeExecute("ALTER TABLE tareas ADD COLUMN IF NOT EXISTS questions_json TEXT");
            safeExecute("ALTER TABLE tareas ADD COLUMN IF NOT EXISTS links_extra_json TEXT DEFAULT '[]'");
            safeExecute("ALTER TABLE tareas ALTER COLUMN estado TYPE VARCHAR(20)");
            safeExecute("ALTER TABLE tareas DROP CONSTRAINT IF EXISTS tareas_estado_check");

            log.info("Migración de columnas completada.");
        } catch (Exception ex) {
            log.warn("Migración de columnas (no crítico): {}", ex.getMessage());
        }
    }

    private void safeExecute(String sql) {
        try {
            entityManager.createNativeQuery(sql).executeUpdate();
        } catch (Exception ex) {
            log.debug("SQL ignorado ({}): {}", sql, ex.getMessage());
        }
    }
}
