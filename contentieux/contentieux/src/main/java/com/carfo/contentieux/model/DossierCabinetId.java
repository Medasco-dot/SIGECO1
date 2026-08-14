package com.carfo.contentieux.model;

import java.io.Serializable;
import java.util.Objects;

public class DossierCabinetId implements Serializable {

    private String numeroDossier;
    private String identifiantCabinet;

    public DossierCabinetId() {}

    public DossierCabinetId(String numeroDossier, String identifiantCabinet) {
        this.numeroDossier = numeroDossier;
        this.identifiantCabinet = identifiantCabinet;
    }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getIdentifiantCabinet() { return identifiantCabinet; }
    public void setIdentifiantCabinet(String identifiantCabinet) { this.identifiantCabinet = identifiantCabinet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DossierCabinetId that = (DossierCabinetId) o;
        return Objects.equals(numeroDossier, that.numeroDossier) && Objects.equals(identifiantCabinet, that.identifiantCabinet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroDossier, identifiantCabinet);
    }
}