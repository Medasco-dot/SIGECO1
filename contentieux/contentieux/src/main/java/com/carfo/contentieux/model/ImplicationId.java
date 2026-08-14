package com.carfo.contentieux.model;

import java.io.Serializable;
import java.util.Objects;

public class ImplicationId implements Serializable {

    private String numeroDossier;
    private Integer idPartie;

    // Constructeur vide obligatoire pour Hibernate
    public ImplicationId() {}

    public ImplicationId(String numeroDossier, Integer idPartie) {
        this.numeroDossier = numeroDossier;
        this.idPartie = idPartie;
    }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public Integer getIdPartie() { return idPartie; }
    public void setIdPartie(Integer idPartie) { this.idPartie = idPartie; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImplicationId that = (ImplicationId) o;
        return Objects.equals(numeroDossier, that.numeroDossier) && Objects.equals(idPartie, that.idPartie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroDossier, idPartie);
    }
}