package com.carfo.contentieux.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final long EXPIRATION_MS = 8L * 60 * 60 * 1000;
    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret:change-me}") String secret) {
        // Hachage en SHA-256 pour garantir une clé de 256 bits quelle que soit la longueur du secret configuré
        this.key = Keys.hmacShaKeyFor(sha256(secret));
    }

    public String generateToken(String identifiant, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(identifiant)
                .claim(ROLE_CLAIM, role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public JwtClaims validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String role = claims.get(ROLE_CLAIM, String.class);
            if (role == null || claims.getId() == null) {
                return null;
            }
            LocalDateTime expiration = claims.getExpiration() != null
                    ? LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault())
                    : LocalDateTime.now().plusHours(8);
            return new JwtClaims(claims.getSubject(), role, claims.getId(), expiration);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    public static class JwtClaims {
        private final String identifiant;
        private final String role;
        private final String jti;
        private final LocalDateTime expiration;

        public JwtClaims(String identifiant, String role, String jti, LocalDateTime expiration) {
            this.identifiant = identifiant;
            this.role = role;
            this.jti = jti;
            this.expiration = expiration;
        }

        public String getIdentifiant() {
            return identifiant;
        }

        public String getRole() {
            return role;
        }

        public String getJti() {
            return jti;
        }

        public LocalDateTime getExpiration() {
            return expiration;
        }
    }
}
