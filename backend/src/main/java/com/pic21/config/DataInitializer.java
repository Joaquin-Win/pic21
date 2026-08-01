/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.config.DataInitializer
 *  com.pic21.domain.Credencial
 *  com.pic21.domain.PerfilPersonal
 *  com.pic21.domain.Rol
 *  com.pic21.domain.Usuario
 *  com.pic21.repository.UsuarioRepository
 *  jakarta.persistence.EntityManager
 *  jakarta.persistence.PersistenceContext
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.ApplicationArguments
 *  org.springframework.boot.ApplicationRunner
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Component
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.config;

import com.pic21.domain.Credencial;
import com.pic21.domain.PerfilPersonal;
import com.pic21.domain.Rol;
import com.pic21.domain.Usuario;
import com.pic21.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.EnumSet;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer
implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    @PersistenceContext
    private EntityManager entityManager;
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@pic21.com";
    private static final String ADMIN_PASSWORD = "Msjj2023!";

    @Transactional
    public void run(ApplicationArguments args) {
        this.migrateColumns();
        this.initAdmin();
    }

    private void initAdmin() {
        Optional existing = this.usuarioRepository.findByUsernameIgnoreCase(ADMIN_USERNAME);
        if (existing.isPresent()) {
            Usuario admin = (Usuario)existing.get();
            boolean changed = false;
            if (!ADMIN_EMAIL.equals(admin.getCredencial().getEmail())) {
                admin.getCredencial().setEmail(ADMIN_EMAIL);
                changed = true;
            }
            if (!this.passwordEncoder.matches((CharSequence)ADMIN_PASSWORD, admin.getCredencial().getPasswordHash())) {
                admin.getCredencial().setPasswordHash(this.passwordEncoder.encode((CharSequence)ADMIN_PASSWORD));
                changed = true;
            }
            if (changed) {
                this.usuarioRepository.save((Object)admin);
                log.info("Credenciales del admin actualizadas.");
            }
            return;
        }
        Credencial credencial = Credencial.builder().email(ADMIN_EMAIL).passwordHash(this.passwordEncoder.encode((CharSequence)ADMIN_PASSWORD)).build();
        PerfilPersonal perfil = PerfilPersonal.builder().dni("00000000").correo(ADMIN_EMAIL).build();
        Usuario admin = Usuario.builder().username(ADMIN_USERNAME).nombre("Admin").apellido("Admin").roles(EnumSet.of(Rol.R04_ADMIN)).activo(true).credencial(credencial).perfilPersonal(perfil).build();
        this.usuarioRepository.save((Object)admin);
        log.info("Usuario admin creado (R04_ADMIN).");
    }

    private void migrateColumns() {
        try {
            this.safeExecute("ALTER TABLE asignaciones_tarea ADD COLUMN IF NOT EXISTS score INTEGER");
            this.safeExecute("ALTER TABLE asignaciones_tarea ADD COLUMN IF NOT EXISTS attempts INTEGER DEFAULT 0");
            this.safeExecute("ALTER TABLE asignaciones_tarea ALTER COLUMN attempts SET DEFAULT 0");
            this.safeExecute("UPDATE asignaciones_tarea SET attempts = 0 WHERE attempts IS NULL");
            this.safeExecute("ALTER TABLE asignaciones_tarea ALTER COLUMN estado TYPE VARCHAR(20)");
            this.safeExecute("ALTER TABLE asignaciones_tarea DROP CONSTRAINT IF EXISTS asignaciones_tarea_estado_check");
            this.safeExecute("ALTER TABLE reuniones ADD COLUMN IF NOT EXISTS news_links_extra_json TEXT DEFAULT '[]'");
            this.safeExecute("ALTER TABLE tareas ADD COLUMN IF NOT EXISTS questions_json TEXT");
            this.safeExecute("ALTER TABLE tareas ADD COLUMN IF NOT EXISTS links_extra_json TEXT DEFAULT '[]'");
            this.safeExecute("ALTER TABLE tareas ALTER COLUMN estado TYPE VARCHAR(20)");
            this.safeExecute("ALTER TABLE tareas DROP CONSTRAINT IF EXISTS tareas_estado_check");
            log.info("Migraci\u00f3n de columnas completada.");
        }
        catch (Exception ex) {
            log.warn("Migraci\u00f3n de columnas (no cr\u00edtico): {}", (Object)ex.getMessage());
        }
    }

    private void safeExecute(String sql) {
        try {
            this.entityManager.createNativeQuery(sql).executeUpdate();
        }
        catch (Exception ex) {
            log.debug("SQL ignorado ({}): {}", (Object)sql, (Object)ex.getMessage());
        }
    }

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
}

