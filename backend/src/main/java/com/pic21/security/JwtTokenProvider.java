/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.security.JwtTokenProvider
 *  io.jsonwebtoken.Claims
 *  io.jsonwebtoken.JwtException
 *  io.jsonwebtoken.Jwts
 *  io.jsonwebtoken.security.Keys
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.stereotype.Component
 */
package com.pic21.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value(value="${app.jwt.secret}")
    private String jwtSecret;
    @Value(value="${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor((byte[])this.jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails)authentication.getPrincipal();
        return this.buildToken(userDetails.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return this.buildToken(username);
    }

    private String buildToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + this.jwtExpiration);
        return Jwts.builder().subject(username).issuedAt(now).expiration(expiryDate).signWith((Key)this.getSigningKey()).compact();
    }

    public String getUsernameFromToken(String token) {
        return this.getClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return this.getClaims(token).getExpiration();
    }

    private Claims getClaims(String token) {
        return (Claims)Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims((CharSequence)token).getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims((CharSequence)token);
            return true;
        }
        catch (JwtException e) {
            log.warn("JWT inv\u00e1lido: {}", (Object)e.getMessage());
        }
        catch (IllegalArgumentException e) {
            log.warn("JWT vac\u00edo o nulo: {}", (Object)e.getMessage());
        }
        return false;
    }
}

