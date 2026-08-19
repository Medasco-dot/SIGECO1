package com.carfo.contentieux.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Journal d'audit immuable : consigne qui a fait quoi, quand, sur quelle entité. Aucune
 * méthode applicative ne permet de modifier ou supprimer une entrée existante — voir
 * AuditLogRepository, qui n'expose volontairement ni update ni delete.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    public enum Action { CREATE, UPDATE, DELETE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "horodatage", nullable = false)
    private LocalDateTime horodatage;

    @Column(name = "identifiant_acteur", nullable = false, length = 50)
    private String identifiantActeur;

    @Column(name = "role_acteur", nullable = false, length = 30)
    private String roleActeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;

    @Column(name = "type_entite", nullable = false, length = 60)
    private String typeEntite;

    @Column(name = "identifiant_entite", nullable = false, length = 60)
    private String identifiantEntite;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    protected AuditLog() {
        // requis par JPA
    }

    public AuditLog(String identifiantActeur, String roleActeur, Action action,
                     String typeEntite, String identifiantEntite, String details) {
        this.horodatage = LocalDateTime.now();
        this.identifiantActeur = identifiantActeur;
        this.roleActeur = roleActeur;
        this.action = action;
        this.typeEntite = typeEntite;
        this.identifiantEntite = identifiantEntite;
        this.details = details;
    }

    public Long getId() { return id; }
    public LocalDateTime getHorodatage() { return horodatage; }
    public String getIdentifiantActeur() { return identifiantActeur; }
    public String getRoleActeur() { return roleActeur; }
    public Action getAction() { return action; }
    public String getTypeEntite() { return typeEntite; }
    public String getIdentifiantEntite() { return identifiantEntite; }
    public String getDetails() { return details; }
}
