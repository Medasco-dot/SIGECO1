package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "IMPLICATION")
public class Implication {

    @EmbeddedId
    private ImplicationId id;

    @ManyToOne
    @MapsId("numeroDossier")
    @JoinColumn(name = "numero_dossier")
    @JsonIgnore
    private Dossier dossier;

    @ManyToOne
    @MapsId("idPartie")
    @JoinColumn(name = "id_partie")
    private Partie partie;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role { demandeur, defendeur }

    @Enumerated(EnumType.STRING)
    @Column(name = "lien_parente")
    private LienParente lienParente;

    public enum LienParente { assure, epoux_epouse, enfant, autre_ayant_droit }

    // Getters et setters
    public ImplicationId getId() { return id; }
    public void setId(ImplicationId id) { this.id = id; }

    @JsonProperty("numeroDossier")
    public String getNumeroDossier() {
        return id != null ? id.getNumeroDossier() : null;
    }

    @JsonProperty("idPartie")
    public Integer getIdPartie() {
        return id != null ? id.getIdPartie() : null;
    }

    @JsonIgnore
    public Dossier getDossier() { return dossier; }
    public void setDossier(Dossier dossier) { this.dossier = dossier; }

    public Partie getPartie() { return partie; }
    public void setPartie(Partie partie) { this.partie = partie; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LienParente getLienParente() { return lienParente; }
    public void setLienParente(LienParente lienParente) { this.lienParente = lienParente; }
}