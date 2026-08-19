package com.carfo.contentieux.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Liste de révocation des jetons JWT : un jeton dont le "jti" figure ici est refusé par
 * {@link com.carfo.contentieux.config.SecurityConfig.JwtAuthenticationFilter} même s'il
 * n'est pas encore expiré (déconnexion explicite, compte compromis).
 */
@Entity
@Table(name = "revoked_token")
public class RevokedToken {

    @Id
    @Column(name = "jti", length = 64)
    private String jti;

    @Column(name = "revoque_le", nullable = false)
    private LocalDateTime revoqueLe;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    protected RevokedToken() {
    }

    public RevokedToken(String jti, LocalDateTime expireLe) {
        this.jti = jti;
        this.revoqueLe = LocalDateTime.now();
        this.expireLe = expireLe;
    }

    public String getJti() { return jti; }
    public LocalDateTime getRevoqueLe() { return revoqueLe; }
    public LocalDateTime getExpireLe() { return expireLe; }
}
