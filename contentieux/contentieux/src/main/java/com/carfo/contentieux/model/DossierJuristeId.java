package com.carfo.contentieux.model;

import java.io.Serializable;
import java.util.Objects;

public class DossierJuristeId implements Serializable {

    private String numeroDossier;
    private String matricule;

    // Constructeur vide obligatoire pour Hibernate
    public DossierJuristeId() {}

    public DossierJuristeId(String numeroDossier, String matricule ) {
        this.numeroDossier = numeroDossier;
        this.matricule = matricule;
    }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DossierJuristeId that = (DossierJuristeId) o;
        return Objects.equals(numeroDossier, that.numeroDossier) && Objects.equals(matricule, that.matricule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroDossier, matricule);
    }
}