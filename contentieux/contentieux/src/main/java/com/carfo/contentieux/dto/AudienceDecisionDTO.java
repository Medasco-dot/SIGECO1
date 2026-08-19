package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.AudienceDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AudienceDecisionDTO {

    private Integer numAudienceDecision;

    @NotNull
    private LocalDate date;

    @Size(max = 100)
    private String lieuAudience;

    @NotNull
    private AudienceDecision.TypeEtape typeEtape;

    private AudienceDecision.NatureDecision natureDecision;

    @Size(max = 2000)
    private String resumeDecision;

    private AudienceDecision.IssuePourCarfo issuePourCarfo;

    @PositiveOrZero
    private BigDecimal montantObtenu;

    @PositiveOrZero
    private BigDecimal montantDu;

    @PositiveOrZero
    private BigDecimal fraisJustice;

    @NotNull
    private Integer etapeDossierId;

    public Integer getNumAudienceDecision() { return numAudienceDecision; }
    public void setNumAudienceDecision(Integer numAudienceDecision) { this.numAudienceDecision = numAudienceDecision; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getLieuAudience() { return lieuAudience; }
    public void setLieuAudience(String lieuAudience) { this.lieuAudience = lieuAudience; }

    public AudienceDecision.TypeEtape getTypeEtape() { return typeEtape; }
    public void setTypeEtape(AudienceDecision.TypeEtape typeEtape) { this.typeEtape = typeEtape; }

    public AudienceDecision.NatureDecision getNatureDecision() { return natureDecision; }
    public void setNatureDecision(AudienceDecision.NatureDecision natureDecision) { this.natureDecision = natureDecision; }

    public String getResumeDecision() { return resumeDecision; }
    public void setResumeDecision(String resumeDecision) { this.resumeDecision = resumeDecision; }

    public AudienceDecision.IssuePourCarfo getIssuePourCarfo() { return issuePourCarfo; }
    public void setIssuePourCarfo(AudienceDecision.IssuePourCarfo issuePourCarfo) { this.issuePourCarfo = issuePourCarfo; }

    public BigDecimal getMontantObtenu() { return montantObtenu; }
    public void setMontantObtenu(BigDecimal montantObtenu) { this.montantObtenu = montantObtenu; }

    public BigDecimal getMontantDu() { return montantDu; }
    public void setMontantDu(BigDecimal montantDu) { this.montantDu = montantDu; }

    public BigDecimal getFraisJustice() { return fraisJustice; }
    public void setFraisJustice(BigDecimal fraisJustice) { this.fraisJustice = fraisJustice; }

    public Integer getEtapeDossierId() { return etapeDossierId; }
    public void setEtapeDossierId(Integer etapeDossierId) { this.etapeDossierId = etapeDossierId; }
}
