package com.carfo.contentieux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DossierConcurrencyIntegrationTest {

    @LocalServerPort
    int port;

    private final RestTemplate rest = new RestTemplate();

    @Test
    public void concurrentCreation_noDuplicates() throws Exception {
        int n = 40;
        ExecutorService ex = Executors.newFixedThreadPool(10);
        List<Future<String>> futures = new ArrayList<>();
        HttpHeaders headers = authHeaders();
        for (int i = 0; i < n; i++) {
            final int idx = i;
            futures.add(ex.submit(() -> {
                Map<String, Object> body = new HashMap<>();
                body.put("dateOuverture", "2026-08-06");
                body.put("nature", "pension_retraite");
                body.put("montantReclame", idx);
                String url = "http://localhost:" + port + "/api/dossiers";
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                ResponseEntity<Map> r = rest.exchange(url, HttpMethod.POST, request, Map.class);
                Object num = r.getBody().get("numeroDossier");
                return num != null ? num.toString() : null;
            }));
        }
        Set<String> nums = new HashSet<>();
        for (Future<String> f : futures) {
            String numero = f.get(60, TimeUnit.SECONDS);
            nums.add(numero);
        }
        assertEquals(n, nums.size(), "Expected all generated numbers to be unique");
        ex.shutdown();
    }

    @Test
    public void createWithNumeroDossierRejected() {
        Map<String, Object> body = new HashMap<>();
        body.put("numeroDossier", "DOS-2026-0001");
        body.put("dateOuverture", "2026-08-06");
        body.put("nature", "pension_retraite");
        body.put("montantReclame", 100);
        String url = "http://localhost:" + port + "/api/dossiers";
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> rest.exchange(url, HttpMethod.POST, authenticatedEntity(body), Map.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        String response = ex.getResponseBodyAsString();
        assert(response.contains("numeroDossier") || response.contains("non autorisé") || response.contains("non autorisées"));
    }

    private HttpHeaders authHeaders() {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("identifiant", "juriste");
        loginPayload.put("motDePasse", "juriste123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> loginResponse = rest.postForEntity("http://localhost:" + port + "/api/auth/login", loginPayload, Map.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = (String) loginResponse.getBody().get("token");
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpEntity<Map<String, Object>> authenticatedEntity(Map<String, Object> body) {
        HttpHeaders headers = authHeaders();
        return new HttpEntity<>(body, headers);
    }
}
