package com.carfo.contentieux.dto;

import jakarta.validation.constraints.NotBlank;

public class DossierJuristeDTO {

    @NotBlank
    private String numeroDossier;

    @NotBlank
    private String matricule;

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
}
