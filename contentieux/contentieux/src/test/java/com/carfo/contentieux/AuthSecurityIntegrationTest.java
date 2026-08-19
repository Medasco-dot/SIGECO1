package com.carfo.contentieux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Vérifie deux mécanismes de durcissement de l'authentification :
 * - la révocation immédiate d'un jeton via /api/auth/logout (sans attendre son expiration) ;
 * - le verrouillage temporaire après plusieurs échecs de connexion consécutifs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthSecurityIntegrationTest {

    @LocalServerPort
    int port;

    private final RestTemplate rest = new RestTemplate();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String login(String identifiant, String motDePasse) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("identifiant", identifiant);
        payload.put("motDePasse", motDePasse);
        ResponseEntity<Map> response = rest.postForEntity(url("/api/auth/login"), payload, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return (String) response.getBody().get("token");
    }

    @Test
    void unJetonRevoqueEstRefuseImmediatement() {
        String token = login("juriste", "juriste123");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // Le jeton fonctionne avant deconnexion
        ResponseEntity<Object[]> avant = rest.exchange(url("/api/dossiers"), HttpMethod.GET, new HttpEntity<>(headers), Object[].class);
        assertEquals(HttpStatus.OK, avant.getStatusCode());

        // Deconnexion : revoque le jeton
        ResponseEntity<Void> logout = rest.postForEntity(url("/api/auth/logout"), new HttpEntity<>(headers), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, logout.getStatusCode());

        // Le meme jeton, pourtant non expire, est desormais refuse
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> rest.exchange(url("/api/dossiers"), HttpMethod.GET, new HttpEntity<>(headers), Object[].class));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void tropDeTentativesEchoueesBloqueTemporairementLeCompte() {
        String identifiant = "brute-force-test-user";
        Map<String, Object> payload = new HashMap<>();
        payload.put("identifiant", identifiant);
        payload.put("motDePasse", "mauvais-mot-de-passe");

        // 5 tentatives echouees (identifiant inconnu, mais le compteur s'applique quand meme)
        for (int i = 0; i < 5; i++) {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> rest.postForEntity(url("/api/auth/login"), payload, Map.class));
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }

        // La 6e tentative est bloquee independamment du mot de passe fourni
        HttpClientErrorException bloque = assertThrows(HttpClientErrorException.class,
                () -> rest.postForEntity(url("/api/auth/login"), payload, Map.class));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, bloque.getStatusCode());
    }
}
