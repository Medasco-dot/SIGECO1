package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DossierSearchCriteria {

    private String numeroDossier;
    private TypeContentieux.Nature typeContentieux;
    private EtapeDossier.Etape etapeCourante;
    private String nomPartie;
    private LocalDate dateOuvertureMin;
    private LocalDate dateOuvertureMax;
    private BigDecimal risqueFinancierMin;
    private BigDecimal risqueFinancierMax;
    private String matriculeJuriste;
    private String identifiantCabinet;

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public TypeContentieux.Nature getTypeContentieux() { return typeContentieux; }
    public void setTypeContentieux(TypeContentieux.Nature typeContentieux) { this.typeContentieux = typeContentieux; }

    public EtapeDossier.Etape getEtapeCourante() { return etapeCourante; }
    public void setEtapeCourante(EtapeDossier.Etape etapeCourante) { this.etapeCourante = etapeCourante; }

    public String getNomPartie() { return nomPartie; }
    public void setNomPartie(String nomPartie) { this.nomPartie = nomPartie; }

    public LocalDate getDateOuvertureMin() { return dateOuvertureMin; }
    public void setDateOuvertureMin(LocalDate dateOuvertureMin) { this.dateOuvertureMin = dateOuvertureMin; }

    public LocalDate getDateOuvertureMax() { return dateOuvertureMax; }
    public void setDateOuvertureMax(LocalDate dateOuvertureMax) { this.dateOuvertureMax = dateOuvertureMax; }

    public BigDecimal getRisqueFinancierMin() { return risqueFinancierMin; }
    public void setRisqueFinancierMin(BigDecimal risqueFinancierMin) { this.risqueFinancierMin = risqueFinancierMin; }

    public BigDecimal getRisqueFinancierMax() { return risqueFinancierMax; }
    public void setRisqueFinancierMax(BigDecimal risqueFinancierMax) { this.risqueFinancierMax = risqueFinancierMax; }

    public String getMatriculeJuriste() { return matriculeJuriste; }
    public void setMatriculeJuriste(String matriculeJuriste) { this.matriculeJuriste = matriculeJuriste; }

    public String getIdentifiantCabinet() { return identifiantCabinet; }
    public void setIdentifiantCabinet(String identifiantCabinet) { this.identifiantCabinet = identifiantCabinet; }
}
