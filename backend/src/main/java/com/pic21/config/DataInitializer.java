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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Inicializa datos necesarios al arrancar la aplicación:
 * - Crea los 4 roles si no existen
 * - Crea o actualiza el usuario administrador
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Contraseña del admin — se aplica al crear o si no coincide. */
    private static final String ADMIN_PASSWORD = "msjj2023";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initRoles();
        initAdmin();
    }

    private void initRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                String desc = switch (roleName) {
                    case ADMIN      -> "Administrador del sistema";
                    case PROFESOR   -> "Profesor de la institución";
                    case AYUDANTE   -> "Ayudante de cátedra";
                    case ESTUDIANTE -> "Estudiante";
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
}
