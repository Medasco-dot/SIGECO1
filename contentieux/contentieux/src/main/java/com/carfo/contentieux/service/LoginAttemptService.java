package com.carfo.contentieux.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Protection anti brute-force sur /api/auth/login : verrouille temporairement un identifiant
 * après un nombre configurable de tentatives infructueuses consécutives. Stockage en mémoire
 * (suffisant pour un déploiement mono-instance ; à externaliser vers un cache partagé —
 * Redis par exemple — en cas de déploiement multi-instances).
 */
@Service
public class LoginAttemptService {

    private record Tentatives(AtomicInteger echecs, Instant premierEchec) {}

    private final ConcurrentHashMap<String, Tentatives> tentativesParIdentifiant = new ConcurrentHashMap<>();

    private final int maxTentatives;
    private final Duration dureeBlocage;

    public LoginAttemptService(@Value("${app.login.max-tentatives:5}") int maxTentatives,
                                @Value("${app.login.blocage-minutes:15}") long blocageMinutes) {
        this.maxTentatives = maxTentatives;
        this.dureeBlocage = Duration.ofMinutes(blocageMinutes);
    }

    public boolean estBloque(String identifiant) {
        Tentatives t = tentativesParIdentifiant.get(cle(identifiant));
        if (t == null) {
            return false;
        }
        if (Instant.now().isAfter(t.premierEchec().plus(dureeBlocage))) {
            tentativesParIdentifiant.remove(cle(identifiant));
            return false;
        }
        return t.echecs().get() >= maxTentatives;
    }

    public void enregistrerEchec(String identifiant) {
        tentativesParIdentifiant.compute(cle(identifiant), (k, existant) -> {
            if (existant == null || Instant.now().isAfter(existant.premierEchec().plus(dureeBlocage))) {
                return new Tentatives(new AtomicInteger(1), Instant.now());
            }
            existant.echecs().incrementAndGet();
            return existant;
        });
    }

    public void enregistrerSucces(String identifiant) {
        tentativesParIdentifiant.remove(cle(identifiant));
    }

    public int minutesAvantDeblocage(String identifiant) {
        Tentatives t = tentativesParIdentifiant.get(cle(identifiant));
        if (t == null) {
            return 0;
        }
        Duration restant = Duration.between(Instant.now(), t.premierEchec().plus(dureeBlocage));
        return (int) Math.max(1, restant.toMinutes() + 1);
    }

    private String cle(String identifiant) {
        return identifiant == null ? "" : identifiant.toLowerCase();
    }
}
