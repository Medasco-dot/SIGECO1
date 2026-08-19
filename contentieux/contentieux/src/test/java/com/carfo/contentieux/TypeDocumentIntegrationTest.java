package com.carfo.contentieux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.Document;
import com.carfo.contentieux.model.Role;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.model.Utilisateur;
import com.carfo.contentieux.repository.DocumentRepository;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.TypeContentieuxRepository;
import com.carfo.contentieux.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le référentiel des types de document était jusqu'ici une liste figée dans le code (aucun
 * nouveau type de contentieux imprévu ne pouvait faire ajouter les pièces qui lui correspondent).
 * Vérifie que le nouveau CRUD couvre le cycle complet : création, doublon refusé, et suppression
 * bloquée tant qu'un document utilise encore le type.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TypeDocumentIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    TypeContentieuxRepository typeContentieuxRepository;
    @Autowired
    DossierRepository dossierRepository;
    @Autowired
    DocumentRepository documentRepository;

    private final RestTemplate rest = new RestTemplate();

    private String login() {
        if (utilisateurRepository.findByIdentifiant("tdoc-juriste").isEmpty()) {
            Utilisateur u = new Utilisateur();
            u.setIdentifiant("tdoc-juriste");
            u.setMotDePasse(passwordEncoder.encode("Test1234!"));
            u.setNom("Test");
            u.setPrenom("TypeDocument");
            u.setRole(Role.juriste);
            utilisateurRepository.save(u);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("identifiant", "tdoc-juriste");
        payload.put("motDePasse", "Test1234!");
        ResponseEntity<Map> response = rest.postForEntity(url("/api/auth/login"), payload, Map.class);
        return (String) response.getBody().get("token");
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void unNouveauTypeDeDocumentEstCreeEtApparaitDansLaListe() {
        String token = login();
        Map<String, Object> body = new HashMap<>();
        body.put("code", "certificat_naissance");
        body.put("libelle", "Certificat de naissance");

        ResponseEntity<Map> create = rest.postForEntity(url("/api/document-types"), new HttpEntity<>(body, bearer(token)), Map.class);
        assertEquals(HttpStatus.CREATED, create.getStatusCode());

        ResponseEntity<Map[]> list = rest.exchange(url("/api/document-types"), HttpMethod.GET, new HttpEntity<>(bearer(token)), Map[].class);
        boolean present = false;
        for (Map<?, ?> t : list.getBody()) {
            if ("certificat_naissance".equals(t.get("code"))) present = true;
        }
        assertTrue(present, "Le nouveau type doit apparaître dans la liste retournée par GET /api/document-types");
    }

    @Test
    void unCodeDejaExistantEstRefuseAvecUnConflit() {
        String token = login();
        Map<String, Object> body = new HashMap<>();
        body.put("code", "type_deja_pris");
        body.put("libelle", "Premier enregistrement");
        ResponseEntity<Map> premier = rest.postForEntity(url("/api/document-types"), new HttpEntity<>(body, bearer(token)), Map.class);
        assertEquals(HttpStatus.CREATED, premier.getStatusCode());

        body.put("libelle", "Doublon volontaire");
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> rest.postForEntity(
                url("/api/document-types"), new HttpEntity<>(body, bearer(token)), Map.class));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void laSuppressionEstRefuseeTantQuUnDocumentUtiliseCeType() {
        String token = login();
        Map<String, Object> typeBody = new HashMap<>();
        typeBody.put("code", "type_en_usage");
        typeBody.put("libelle", "Type en cours d'utilisation");
        rest.postForEntity(url("/api/document-types"), new HttpEntity<>(typeBody, bearer(token)), Map.class);

        TypeContentieux tc = new TypeContentieux();
        tc.setNature(TypeContentieux.Nature.autre);
        tc = typeContentieuxRepository.save(tc);

        Dossier d = new Dossier();
        d.setNumeroDossier("TDOC" + (System.nanoTime() % 100000));
        d.setTypeContentieux(tc);
        d.setDateOuverture(LocalDate.now());
        d = dossierRepository.save(d);

        Document doc = new Document();
        doc.setDossier(d);
        doc.setTypeDocument("type_en_usage");
        doc.setDateAjout(LocalDate.now());
        documentRepository.save(doc);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> rest.exchange(
                url("/api/document-types/type_en_usage"), HttpMethod.DELETE, new HttpEntity<>(bearer(token)), Void.class));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}
