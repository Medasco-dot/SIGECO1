package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "DOSSIER")
public class Dossier {

    @Id
    @Column(name = "numero_dossier", length = 15)
    private String numeroDossier;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDate dateOuverture;

    @Column(name = "resume_affaire", length = 2000)
    private String resumeAffaire;

    @Column(name = "observation", length = 2000)
    private String observation;

    @Column(name = "risque_financier", precision = 18, scale = 2)
    private BigDecimal risqueFinancier;

    @Column(name = "montant_reclame", precision = 18, scale = 2)
    private BigDecimal montantReclame;

    @Column(name = "frais_justice", precision = 18, scale = 2)
    private BigDecimal fraisJustice;

    @ManyToOne
    @JoinColumn(name = "num_contentieux", nullable = false)
    private TypeContentieux typeContentieux;

    @OneToMany(mappedBy = "dossier")
    @JsonIgnore
    private List<AudienceDecision> audienceDecisions;

    @OneToMany(mappedBy = "dossier")
    @JsonIgnore
    private List<Document> documents;

    @OneToMany(mappedBy = "dossier")
    @JsonIgnore
    private List<EtapeDossier> etapes;

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public LocalDate getDateOuverture() { return dateOuverture; }
    public void setDateOuverture(LocalDate dateOuverture) { this.dateOuverture = dateOuverture; }

    public String getResumeAffaire() { return resumeAffaire; }
    public void setResumeAffaire(String resumeAffaire) { this.resumeAffaire = resumeAffaire; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public BigDecimal getRisqueFinancier() { return risqueFinancier; }
    public void setRisqueFinancier(BigDecimal risqueFinancier) { this.risqueFinancier = risqueFinancier; }

    public BigDecimal getMontantReclame() { return montantReclame; }
    public void setMontantReclame(BigDecimal montantReclame) { this.montantReclame = montantReclame; }

    public BigDecimal getFraisJustice() { return fraisJustice; }
    public void setFraisJustice(BigDecimal fraisJustice) { this.fraisJustice = fraisJustice; }

    public TypeContentieux getTypeContentieux() { return typeContentieux; }
    public void setTypeContentieux(TypeContentieux typeContentieux) { this.typeContentieux = typeContentieux; }

    public List<AudienceDecision> getAudienceDecisions() { return audienceDecisions; }
    public void setAudienceDecisions(List<AudienceDecision> audienceDecisions) { this.audienceDecisions = audienceDecisions; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    public List<EtapeDossier> getEtapes() { return etapes; }
    public void setEtapes(List<EtapeDossier> etapes) { this.etapes = etapes; }

    @Transient
    public EtapeDossier.Etape getEtapeCourante() {
        if (etapes == null || etapes.isEmpty()) {
            return null;
        }
        return etapes.stream()
                .filter(etape -> etape != null && etape.getDateFin() == null)
                .sorted(Comparator.comparing(EtapeDossier::getDateDebut, Comparator.nullsLast(LocalDate::compareTo)).reversed())
                .map(EtapeDossier::getEtape)
                .findFirst()
                .orElse(null);
    }
}
