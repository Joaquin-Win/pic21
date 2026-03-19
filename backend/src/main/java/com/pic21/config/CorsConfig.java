package com.pic21.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de CORS para PIC21.
 *
 * Orígenes permitidos:
 *  - localhost:* (desarrollo)
 *  - CORS_ALLOWED_ORIGIN (variable de entorno — producción)
 *  - pic21.fly.dev (producción por defecto)
 */
@Configuration
public class CorsConfig {

    /**
     * Origen adicional permitido — configurable via variable de entorno CORS_ALLOWED_ORIGIN.
     * Por defecto: https://pic21.fly.dev
     */
    @Value("${cors.allowed-origin:https://pic21.fly.dev}")
    private String extraAllowedOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Construir lista de orígenes permitidos
        List<String> allowedOrigins = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "null"                          // file:// protocol (desarrollo)
        ));

        // Agregar el origen de producción (Fly.io u otro configurado por env var)
        if (extraAllowedOrigin != null && !extraAllowedOrigin.isBlank()) {
            allowedOrigins.add(extraAllowedOrigin);
        }

        config.setAllowedOriginPatterns(allowedOrigins);

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers permitidos en las requests
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Headers expuestos en la response
        config.setExposedHeaders(List.of("Authorization"));

        // Permitir cookies/credenciales
        config.setAllowCredentials(true);

        // Tiempo de cacheo del preflight (1 hora)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
