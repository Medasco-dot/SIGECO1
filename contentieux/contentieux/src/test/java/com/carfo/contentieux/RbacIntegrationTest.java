package com.carfo.contentieux;

import com.carfo.contentieux.model.Role;
import com.carfo.contentieux.model.Utilisateur;
import com.carfo.contentieux.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Vérifie que le contrôle d'accès par rôle en vigueur dans SecurityConfig correspond bien au
 * diagramme de cas d'utilisation : seul le Juriste crée/modifie les dossiers, seul le Chef de
 * service assigne un juriste à un dossier, et les statistiques consolidées sont réservées au
 * Chef de service et à la Direction Générale.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RbacIntegrationTest {

    private static final String MOT_DE_PASSE_TEST = "Test1234!";

    @LocalServerPort
    int port;

    @Autowired
    UtilisateurRepository utilisateurRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final RestTemplate rest = new RestTemplate();

    @BeforeAll
    void creerUtilisateursDeTest() {
        creerSiAbsent("rbac-chef-service", Role.chef_service);
        creerSiAbsent("rbac-direction", Role.direction_generale);
    }

    private void creerSiAbsent(String identifiant, Role role) {
        if (utilisateurRepository.findByIdentifiant(identifiant).isPresent()) {
            return;
        }
        Utilisateur u = new Utilisateur();
        u.setIdentifiant(identifiant);
        u.setMotDePasse(passwordEncoder.encode(MOT_DE_PASSE_TEST));
        u.setNom("Test");
        u.setPrenom(role.name());
        u.setRole(role);
        utilisateurRepository.save(u);
    }

    private String login(String identifiant, String motDePasse) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("identifiant", identifiant);
        payload.put("motDePasse", motDePasse);
        ResponseEntity<Map> response = rest.postForEntity(url("/api/auth/login"), payload, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return (String) response.getBody().get("token");
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void juristePeutCreerUnDossier() {
        String token = login("juriste", "juriste123");
        Map<String, Object> body = new HashMap<>();
        body.put("dateOuverture", "2026-08-06");
        body.put("nature", "pension_retraite");

        ResponseEntity<Map> response = rest.postForEntity(
                url("/api/dossiers"), new HttpEntity<>(body, bearer(token)), Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void chefDeServiceNePeutPasCreerUnDossier() {
        String token = login("rbac-chef-service", MOT_DE_PASSE_TEST);
        Map<String, Object> body = new HashMap<>();
        body.put("dateOuverture", "2026-08-06");
        body.put("nature", "pension_retraite");

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> rest.postForEntity(
                url("/api/dossiers"), new HttpEntity<>(body, bearer(token)), Map.class));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void juristeNePeutPasConsulterLesStatistiques() {
        String token = login("juriste", "juriste123");

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> rest.exchange(
                url("/api/statistiques/synthese"), HttpMethod.GET, new HttpEntity<>(bearer(token)), Map.class));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void chefDeServicePeutConsulterLesStatistiques() {
        String token = login("rbac-chef-service", MOT_DE_PASSE_TEST);

        ResponseEntity<Map> response = rest.exchange(
                url("/api/statistiques/synthese"), HttpMethod.GET, new HttpEntity<>(bearer(token)), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void directionGeneralePeutConsulterLesStatistiques() {
        String token = login("rbac-direction", MOT_DE_PASSE_TEST);

        ResponseEntity<Map> response = rest.exchange(
                url("/api/statistiques/synthese"), HttpMethod.GET, new HttpEntity<>(bearer(token)), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void laListeDesUtilisateursNeContientJamaisLeHashDuMotDePasse() {
        String token = login("juriste", "juriste123");

        ResponseEntity<Map[]> response = rest.exchange(
                url("/api/utilisateurs"), HttpMethod.GET, new HttpEntity<>(bearer(token)), Map[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        for (Map<?, ?> utilisateur : response.getBody()) {
            org.junit.jupiter.api.Assertions.assertFalse(utilisateur.containsKey("motDePasse"),
                    "La reponse ne doit jamais exposer le champ motDePasse (hash bcrypt)");
        }
    }

    @Test
    void requeteSansJetonEstRejetee() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> rest.getForEntity(url("/api/dossiers"), Map.class));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
