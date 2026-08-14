package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "DOSSIER_JURISTE")
public class DossierJuriste {

    @EmbeddedId
    private DossierJuristeId id;

    @ManyToOne
    @MapsId("numeroDossier")
    @JoinColumn(name = "numero_dossier")
    @JsonIgnore
    private Dossier dossier;

    @ManyToOne
    @MapsId("matricule")
    @JoinColumn(name = "matricule")
    private Juriste juriste;

    @com.fasterxml.jackson.annotation.JsonProperty("numeroDossier")
    public String getNumeroDossier() {
        return id != null ? id.getNumeroDossier() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("matricule")
    public String getMatricule() {
        return id != null ? id.getMatricule() : null;
    }

    // Getters et setters

    public Juriste getJuriste() {return juriste;}
    public void setJuriste(Juriste juriste) {this.juriste = juriste;}

    @JsonIgnore
    public Dossier getDossier() {return dossier;}
    public void setDossier(Dossier dossier) {this.dossier = dossier;}

    public DossierJuristeId getId() {return id;}
    public void setId(DossierJuristeId id) {this.id = id;}
}