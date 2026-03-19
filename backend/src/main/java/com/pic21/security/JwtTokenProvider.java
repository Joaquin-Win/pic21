package com.pic21.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Componente encargado de generar, validar y extraer información de los JWT.
 * Usa jjwt 0.12.x con algoritmo HS256.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // ---------------------------------------------------------
    // Key de firma
    // ---------------------------------------------------------

    private SecretKey getSigningKey() {
        // Usamos los bytes UTF-8 del secret (mínimo 256 bits = 32 chars para HS256)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------
    // Generación de token
    // ---------------------------------------------------------

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return buildToken(username);
    }

    private String buildToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // ---------------------------------------------------------
    // Extracción de claims
    // ---------------------------------------------------------

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ---------------------------------------------------------
    // Validación
    // ---------------------------------------------------------

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT vacío o nulo: {}", e.getMessage());
        }
        return false;
    }
}
