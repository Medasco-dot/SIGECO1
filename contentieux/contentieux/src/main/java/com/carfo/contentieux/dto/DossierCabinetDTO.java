package com.carfo.contentieux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DossierCabinetDTO {

    @NotBlank
    private String numeroDossier;

    @NotBlank
    private String identifiantCabinet;

    @Size(max = 100)
    private String nomAvocatReferent;

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getIdentifiantCabinet() { return identifiantCabinet; }
    public void setIdentifiantCabinet(String identifiantCabinet) { this.identifiantCabinet = identifiantCabinet; }

    public String getNomAvocatReferent() { return nomAvocatReferent; }
    public void setNomAvocatReferent(String nomAvocatReferent) { this.nomAvocatReferent = nomAvocatReferent; }
}
