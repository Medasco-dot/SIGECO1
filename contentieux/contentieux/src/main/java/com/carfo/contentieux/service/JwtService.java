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
import java.util.Date;

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
            if (role == null) {
                return null;
            }
            return new JwtClaims(claims.getSubject(), role);
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

        public JwtClaims(String identifiant, String role) {
            this.identifiant = identifiant;
            this.role = role;
        }

        public String getIdentifiant() {
            return identifiant;
        }

        public String getRole() {
            return role;
        }
    }
}
