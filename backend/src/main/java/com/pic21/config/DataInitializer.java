package com.pic21.config;

import com.pic21.domain.Role;
import com.pic21.domain.User;
import com.pic21.repository.RoleRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Inicializa datos necesarios al arrancar la aplicación:
 * - Crea los 4 roles si no existen
 * - Crea o actualiza el usuario administrador
 * - Migra columnas de DB si faltan (quiz: score, attempts, questions_json)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    /** Contraseña del admin — se aplica al crear o si no coincide. */
    private static final String ADMIN_PASSWORD = "msjj2023";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initRoles();
        initAdmin();
        migrateQuizColumns();
    }

    private void initRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                String desc = switch (roleName) {
                    case ADMIN      -> "Administrador del sistema";
                    case PROFESOR   -> "Profesor de la institución";
                    case AYUDANTE   -> "Ayudante de cátedra";
                    case ESTUDIANTE -> "Estudiante";
                    case EGRESADO   -> "Egresado";
                };
                roleRepository.save(new Role(roleName, desc));
                log.info("Rol creado: {}", roleName);
            }
        }
    }

    private void initAdmin() {
        Optional<User> existing = userRepository.findByUsername("admin");
        if (existing.isPresent()) {
            User admin = existing.get();
            // Actualizar la contraseña si cambió
            if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                userRepository.save(admin);
                log.info("Contraseña del admin actualizada.");
            } else {
                log.debug("Admin ya existe con la contraseña correcta, omitiendo.");
            }
            return;
        }

        // Si el admin no existe, crearlo
        Role role = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User admin = User.builder()
                .username("admin")
                .email("admin@pic21.com")
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .firstName("Admin")
                .lastName("Admin")
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(admin);
        log.info("Usuario admin creado.");
    }

    /**
     * Asegura que las columnas de quiz existan en la DB de producción.
     * Hibernate ddl-auto=update a veces no agrega columnas o las crea con NOT NULL
     * sin default, causando errores en registros pre-existentes.
     */
    private void migrateQuizColumns() {
        try {
            // task_assignments: score (nullable int), attempts (default 0)
            safeExecute("ALTER TABLE task_assignments ADD COLUMN IF NOT EXISTS score INTEGER");
            safeExecute("ALTER TABLE task_assignments ADD COLUMN IF NOT EXISTS attempts INTEGER DEFAULT 0");
            safeExecute("ALTER TABLE task_assignments ALTER COLUMN attempts SET DEFAULT 0");
            safeExecute("ALTER TABLE task_assignments ALTER COLUMN attempts DROP NOT NULL");
            safeExecute("UPDATE task_assignments SET attempts = 0 WHERE attempts IS NULL");

            // Ensure status column accepts APPROVED (expand to VARCHAR(20) if needed)
            safeExecute("ALTER TABLE task_assignments ALTER COLUMN status TYPE VARCHAR(20)");

            // Drop any CHECK constraint on status that might block APPROVED
            // PostgreSQL auto-generates constraint names like: task_assignments_status_check
            safeExecute("ALTER TABLE task_assignments DROP CONSTRAINT IF EXISTS task_assignments_status_check");

            // tasks: questions_json (TEXT)
            safeExecute("ALTER TABLE tasks ADD COLUMN IF NOT EXISTS questions_json TEXT");

            // tasks: ensure legacy status column also accepts APPROVED
            safeExecute("ALTER TABLE tasks ALTER COLUMN status TYPE VARCHAR(20)");
            safeExecute("ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_status_check");

            log.info("Migración de columnas de quiz completada.");
        } catch (Exception ex) {
            // En H2 (dev) el IF NOT EXISTS puede no funcionar — no es crítico
            log.warn("Migración de quiz columns (no crítico): {}", ex.getMessage());
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

