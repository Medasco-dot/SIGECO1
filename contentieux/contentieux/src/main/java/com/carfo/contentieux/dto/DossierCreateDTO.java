package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.TypeContentieux;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = false)
public class DossierCreateDTO {

    private String numeroDossier;

    @NotNull(message = "La date d'ouverture est obligatoire")
    private LocalDate dateOuverture;

    @Size(max = 2000, message = "Le résumé d'affaire est trop long")
    private String resumeAffaire;

    @Size(max = 2000, message = "L'observation est trop longue")
    private String observation;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal risqueFinancier;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal montantReclame;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal fraisJustice;

    private Integer numContentieux;

    private TypeContentieux.Nature nature;

    public String getNumeroDossier() {
        return numeroDossier;
    }

    public void setNumeroDossier(String numeroDossier) {
        if (numeroDossier != null && !numeroDossier.isBlank()) {
            throw new IllegalArgumentException("Le champ numeroDossier ne doit pas être fourni lors de la création d'un dossier.");
        }
        this.numeroDossier = numeroDossier;
    }

    public LocalDate getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(LocalDate dateOuverture) {
        this.dateOuverture = dateOuverture;
    }

    public String getResumeAffaire() {
        return resumeAffaire;
    }

    public void setResumeAffaire(String resumeAffaire) {
        this.resumeAffaire = resumeAffaire;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public BigDecimal getRisqueFinancier() {
        return risqueFinancier;
    }

    public void setRisqueFinancier(BigDecimal risqueFinancier) {
        this.risqueFinancier = risqueFinancier;
    }

    public BigDecimal getMontantReclame() {
        return montantReclame;
    }

    public void setMontantReclame(BigDecimal montantReclame) {
        this.montantReclame = montantReclame;
    }

    public BigDecimal getFraisJustice() {
        return fraisJustice;
    }

    public void setFraisJustice(BigDecimal fraisJustice) {
        this.fraisJustice = fraisJustice;
    }

    public Integer getNumContentieux() {
        return numContentieux;
    }

    public void setNumContentieux(Integer numContentieux) {
        this.numContentieux = numContentieux;
    }

    public TypeContentieux.Nature getNature() {
        return nature;
    }

    public void setNature(TypeContentieux.Nature nature) {
        this.nature = nature;
    }
}
