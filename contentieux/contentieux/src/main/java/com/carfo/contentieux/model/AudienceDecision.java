package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "AUDIENCE_DECISION")
public class AudienceDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_audience_decision")
    private Integer numAudienceDecision;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "lieu_audience", length = 100)
    private String lieuAudience;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_etape", nullable = false)
    private TypeEtape typeEtape;

    public enum TypeEtape {
        premiere_instance, appel, cassation
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_decision")
    private NatureDecision natureDecision;

    public enum NatureDecision { jugement, arret, ordonnance }

    @Column(name = "resume_decision")
    private String resumeDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_pour_carfo")
    private IssuePourCarfo issuePourCarfo;

    public enum IssuePourCarfo { favorable, defavorable, partiellement_favorable }

    @Column(name = "montant_obtenu")
    private BigDecimal montantObtenu;

    @Column(name = "montant_du")
    private BigDecimal montantDu;

    @Column(name = "frais_justice")
    private BigDecimal fraisJustice;

    @ManyToOne
    @JoinColumn(name = "numero_dossier", nullable = false)
    @JsonIgnore
    private Dossier dossier;

    @JsonProperty("numeroDossier")
    @Transient
    public String getNumeroDossier() {
        return dossier != null ? dossier.getNumeroDossier() : null;
    }

    public Integer getNumAudienceDecision() { return numAudienceDecision; }
    public void setNumAudienceDecision(Integer numAudienceDecision) { this.numAudienceDecision = numAudienceDecision; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getLieuAudience() { return lieuAudience; }
    public void setLieuAudience(String lieuAudience) { this.lieuAudience = lieuAudience; }

    public TypeEtape getTypeEtape() { return typeEtape; }
    public void setTypeEtape(TypeEtape typeEtape) { this.typeEtape = typeEtape; }

    public NatureDecision getNatureDecision() { return natureDecision; }
    public void setNatureDecision(NatureDecision natureDecision) { this.natureDecision = natureDecision; }

    public String getResumeDecision() { return resumeDecision; }
    public void setResumeDecision(String resumeDecision) { this.resumeDecision = resumeDecision; }

    public IssuePourCarfo getIssuePourCarfo() { return issuePourCarfo; }
    public void setIssuePourCarfo(IssuePourCarfo issuePourCarfo) { this.issuePourCarfo = issuePourCarfo; }

    public BigDecimal getMontantObtenu() { return montantObtenu; }
    public void setMontantObtenu(BigDecimal montantObtenu) { this.montantObtenu = montantObtenu; }

    public BigDecimal getMontantDu() { return montantDu; }
    public void setMontantDu(BigDecimal montantDu) { this.montantDu = montantDu; }

    public BigDecimal getFraisJustice() { return fraisJustice; }
    public void setFraisJustice(BigDecimal fraisJustice) { this.fraisJustice = fraisJustice; }

    @JsonIgnore
    public Dossier getDossier() { return dossier; }
    public void setDossier(Dossier dossier) { this.dossier = dossier; }
}
