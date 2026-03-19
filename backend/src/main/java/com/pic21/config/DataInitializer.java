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
import java.util.Set;

/**
 * Inicializa datos necesarios al arrancar la aplicación:
 * - Crea los 4 roles si no existen
 * - Crea el usuario administrador por defecto si no existe
 *
 * Usuario: admin / Contraseña: admin123
 * IMPORTANTE: Cambiar la contraseña en producción.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initRoles();
        initUsers();
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

    private void initUsers() {
        createUserIfNotFound("admin", "admin@pic21.com", "admin123", "Admin", "Admin", Role.RoleName.ADMIN);
        createUserIfNotFound("profesor", "profesor@pic21.com", "prof123", "Profesor", "Test", Role.RoleName.PROFESOR);
        createUserIfNotFound("ayudante", "ayudante@pic21.com", "ayu123", "Ayudante", "Test", Role.RoleName.AYUDANTE);
        createUserIfNotFound("estudiante", "estudiante@pic21.com", "est123", "Estudiante", "Test", Role.RoleName.ESTUDIANTE);
    }

    private void createUserIfNotFound(String username, String email, String password, String firstName, String lastName, Role.RoleName roleName) {
        if (userRepository.existsByUsername(username)) {
            log.debug("Usuario {} ya existe, omitiendo.", username);
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rol " + roleName + " no encontrado"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info("Usuario {} creado como {}", username, roleName);
    }
}
