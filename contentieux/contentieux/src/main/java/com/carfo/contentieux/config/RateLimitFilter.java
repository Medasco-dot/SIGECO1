package com.carfo.contentieux.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite de débit générale sur l'API (fenêtre glissante simplifiée par minute, par client :
 * utilisateur authentifié si connu, sinon adresse IP). Complète la protection anti-brute-force
 * dédiée à /api/auth/login (cf. LoginAttemptService) par une garde-fou plus large contre les
 * abus (scripts en boucle, appels excessifs). Stockage en mémoire — a externaliser vers un
 * cache partagé en cas de déploiement multi-instances.
 */
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private record Fenetre(AtomicInteger compteur, Instant debut) {}

    private final ConcurrentHashMap<String, Fenetre> fenetresParClient = new ConcurrentHashMap<>();
    private final int limiteParMinute;

    public RateLimitFilter(@Value("${app.rate-limit.requetes-par-minute:120}") int limiteParMinute) {
        this.limiteParMinute = limiteParMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String client = identifiantClient(request);
        Fenetre fenetre = fenetresParClient.compute(client, (k, existante) -> {
            if (existante == null || Instant.now().isAfter(existante.debut().plusSeconds(60))) {
                return new Fenetre(new AtomicInteger(1), Instant.now());
            }
            existante.compteur().incrementAndGet();
            return existante;
        });

        if (fenetre.compteur().get() > limiteParMinute) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"detail\":\"Trop de requetes. Veuillez ralentir.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String identifiantClient(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // Regroupe par jeton plutot que par IP quand l'utilisateur est identifie
            // (evite qu'un reseau d'entreprise partageant une IP publique soit limite globalement).
            return "token:" + authorization.substring(7).hashCode();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
