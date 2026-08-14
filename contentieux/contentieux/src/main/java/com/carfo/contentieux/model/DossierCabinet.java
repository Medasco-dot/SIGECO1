package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "DOSSIER_CABINET")
public class DossierCabinet {

    @EmbeddedId
    private DossierCabinetId id;

    @ManyToOne
    @MapsId("numeroDossier")
    @JoinColumn(name = "numero_dossier")
    @JsonIgnore
    private Dossier dossier;

    @ManyToOne
    @MapsId("identifiantCabinet")
    @JoinColumn(name = "identifiant_cabinet")
    private Cabinet cabinet;

    @Column(name = "nom_avocat_referent")
    private String nomAvocatReferent;

    @com.fasterxml.jackson.annotation.JsonProperty("numeroDossier")
    public String getNumeroDossier() {
        return id != null ? id.getNumeroDossier() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("identifiantCabinet")
    public String getIdentifiantCabinet() {
        return id != null ? id.getIdentifiantCabinet() : null;
    }

    // Getters et setters
    public DossierCabinetId getId() { return id; }
    public void setId(DossierCabinetId id) { this.id = id; }

    @JsonIgnore
    public Dossier getDossier() { return dossier; }
    public void setDossier(Dossier dossier) { this.dossier = dossier; }

    public Cabinet getCabinet() { return cabinet; }
    public void setCabinet(Cabinet cabinet) { this.cabinet = cabinet; }

    public String getNomAvocatReferent() { return nomAvocatReferent; }
    public void setNomAvocatReferent(String nomAvocatReferent) { this.nomAvocatReferent = nomAvocatReferent; }
}