/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.config.SecurityConfig
 *  com.pic21.security.JwtAuthenticationFilter
 *  jakarta.servlet.Filter
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Lazy
 *  org.springframework.http.HttpMethod
 *  org.springframework.http.HttpStatus
 *  org.springframework.security.authentication.AuthenticationManager
 *  org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
 *  org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 *  org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.config.annotation.web.configurers.HeadersConfigurer$FrameOptionsConfig
 *  org.springframework.security.config.http.SessionCreationPolicy
 *  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.security.web.AuthenticationEntryPoint
 *  org.springframework.security.web.SecurityFilterChain
 *  org.springframework.security.web.authentication.HttpStatusEntryPoint
 *  org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 *  org.springframework.web.cors.CorsConfigurationSource
 */
package com.pic21.config;

import com.pic21.security.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled=true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter, @Qualifier(value="corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(this.corsConfigurationSource)).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(ex -> ex.authenticationEntryPoint((AuthenticationEntryPoint)new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))).authorizeHttpRequests(auth -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(HttpMethod.POST, new String[]{"/api/auth/login"})).permitAll().requestMatchers(HttpMethod.POST, new String[]{"/api/auth/register-public"})).permitAll().requestMatchers(new String[]{"/error"})).permitAll().requestMatchers(new String[]{"/", "/index.html", "/css/**", "/js/**", "/img/**", "/*.ico", "/*.png", "/*.svg"})).permitAll().anyRequest()).authenticated()).headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)).addFilterBefore((Filter)this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return (SecurityFilterChain)http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

