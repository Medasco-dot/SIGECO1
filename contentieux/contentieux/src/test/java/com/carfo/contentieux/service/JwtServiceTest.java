package com.carfo.contentieux.service;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le JWT s'appuie sur la librairie standard jjwt (io.jsonwebtoken) plutôt qu'une
 * implémentation maison. Cette suite verifie les points sensibles : signature,
 * expiration, et rejet des jetons falsifiés ou d'un rôle absent.
 */
class JwtServiceTest {

    private final JwtService jwtService = new JwtService("test-secret-suffisamment-long-pour-hmac");

    @Test
    void genereEtValideUnJetonCorrectement() {
        String token = jwtService.generateToken("juriste", "juriste");
        JwtService.JwtClaims claims = jwtService.validateToken(token);

        assertNotNull(claims);
        assertEquals("juriste", claims.getIdentifiant());
        assertEquals("juriste", claims.getRole());
    }

    @Test
    void rejetteUnJetonSigneAvecUnAutreSecret() {
        String token = jwtService.generateToken("juriste", "juriste");
        JwtService autreService = new JwtService("un-secret-totalement-different");

        assertNull(autreService.validateToken(token));
    }

    @Test
    void rejetteUnJetonDontLaSignatureAEteAlteree() {
        String token = jwtService.generateToken("juriste", "juriste");
        String[] parts = token.split("\\.");
        char last = parts[2].charAt(parts[2].length() - 1);
        char remplacement = last == 'A' ? 'B' : 'A';
        String signatureAlteree = parts[2].substring(0, parts[2].length() - 1) + remplacement;
        String tokenAltere = parts[0] + "." + parts[1] + "." + signatureAlteree;

        assertNull(jwtService.validateToken(tokenAltere));
    }

    @Test
    void rejetteUnJetonMalforme() {
        assertNull(jwtService.validateToken("ceci-nest-pas-un-jwt"));
        assertNull(jwtService.validateToken(""));
        assertNull(jwtService.validateToken(null));
    }

    @Test
    void rejetteUnJetonExpire() throws Exception {
        Field keyField = JwtService.class.getDeclaredField("key");
        keyField.setAccessible(true);
        SecretKey key = (SecretKey) keyField.get(jwtService);

        Date passe = new Date(System.currentTimeMillis() - 60_000);
        String tokenExpire = Jwts.builder()
                .subject("juriste")
                .claim("role", "juriste")
                .issuedAt(new Date(passe.getTime() - 1000))
                .expiration(passe)
                .signWith(key)
                .compact();

        assertNull(jwtService.validateToken(tokenExpire));
    }

    @Test
    void rejetteUnJetonSansRole() throws Exception {
        Field keyField = JwtService.class.getDeclaredField("key");
        keyField.setAccessible(true);
        SecretKey key = (SecretKey) keyField.get(jwtService);

        String tokenSansRole = Jwts.builder()
                .subject("juriste")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertNull(jwtService.validateToken(tokenSansRole));
    }
}
