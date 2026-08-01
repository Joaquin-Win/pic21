/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.config.CorsConfig
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.cors.CorsConfiguration
 *  org.springframework.web.cors.CorsConfigurationSource
 *  org.springframework.web.cors.UrlBasedCorsConfigurationSource
 */
package com.pic21.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Value(value="${cors.allowed-origin:https://pic21.fly.dev}")
    private String extraAllowedOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        ArrayList<String> allowedOrigins = new ArrayList<String>(List.of("http://localhost:*", "http://127.0.0.1:*", "https://*.ngrok-free.dev", "https://*.ngrok.io", "https://*.ngrok.app"));
        if (this.extraAllowedOrigin != null && !this.extraAllowedOrigin.isBlank()) {
            allowedOrigins.add(this.extraAllowedOrigin);
        }
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "ngrok-skip-browser-warning"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(Boolean.valueOf(true));
        config.setMaxAge(Long.valueOf(3600L));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

