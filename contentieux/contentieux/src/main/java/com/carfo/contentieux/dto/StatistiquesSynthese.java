package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StatistiquesSynthese {

    private long totalDossiers;
    private long dossiersOuverts;
    private long dossiersEnAppel;
    private long dossiersEnCassation;
    private long dossiersAlerte;

    private BigDecimal risqueFinancierCumule;
    private BigDecimal fraisJusticeCumules;
    private BigDecimal montantReclameCumule;

    private Map<TypeContentieux.Nature, Long> repartitionParType;
    private Map<EtapeDossier.Etape, Long> repartitionParEtape;

    private List<DossierLeger> top5Risques;

    public long getTotalDossiers() { return totalDossiers; }
    public void setTotalDossiers(long totalDossiers) { this.totalDossiers = totalDossiers; }

    public long getDossiersOuverts() { return dossiersOuverts; }
    public void setDossiersOuverts(long dossiersOuverts) { this.dossiersOuverts = dossiersOuverts; }

    public long getDossiersEnAppel() { return dossiersEnAppel; }
    public void setDossiersEnAppel(long dossiersEnAppel) { this.dossiersEnAppel = dossiersEnAppel; }

    public long getDossiersEnCassation() { return dossiersEnCassation; }
    public void setDossiersEnCassation(long dossiersEnCassation) { this.dossiersEnCassation = dossiersEnCassation; }

    public long getDossiersAlerte() { return dossiersAlerte; }
    public void setDossiersAlerte(long dossiersAlerte) { this.dossiersAlerte = dossiersAlerte; }

    public BigDecimal getRisqueFinancierCumule() { return risqueFinancierCumule; }
    public void setRisqueFinancierCumule(BigDecimal risqueFinancierCumule) { this.risqueFinancierCumule = risqueFinancierCumule; }

    public BigDecimal getFraisJusticeCumules() { return fraisJusticeCumules; }
    public void setFraisJusticeCumules(BigDecimal fraisJusticeCumules) { this.fraisJusticeCumules = fraisJusticeCumules; }

    public BigDecimal getMontantReclameCumule() { return montantReclameCumule; }
    public void setMontantReclameCumule(BigDecimal montantReclameCumule) { this.montantReclameCumule = montantReclameCumule; }

    public Map<TypeContentieux.Nature, Long> getRepartitionParType() { return repartitionParType; }
    public void setRepartitionParType(Map<TypeContentieux.Nature, Long> repartitionParType) { this.repartitionParType = repartitionParType; }

    public Map<EtapeDossier.Etape, Long> getRepartitionParEtape() { return repartitionParEtape; }
    public void setRepartitionParEtape(Map<EtapeDossier.Etape, Long> repartitionParEtape) { this.repartitionParEtape = repartitionParEtape; }

    public List<DossierLeger> getTop5Risques() { return top5Risques; }
    public void setTop5Risques(List<DossierLeger> top5Risques) { this.top5Risques = top5Risques; }

    public static class DossierLeger {
        private String numeroDossier;
        private String typeContentieux;
        private BigDecimal risqueFinancier;
        private String etapeCourante;

        public DossierLeger() {}
        public DossierLeger(String numeroDossier, String typeContentieux,
                            BigDecimal risqueFinancier, String etapeCourante) {
            this.numeroDossier = numeroDossier;
            this.typeContentieux = typeContentieux;
            this.risqueFinancier = risqueFinancier;
            this.etapeCourante = etapeCourante;
        }

        public String getNumeroDossier() { return numeroDossier; }
        public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

        public String getTypeContentieux() { return typeContentieux; }
        public void setTypeContentieux(String typeContentieux) { this.typeContentieux = typeContentieux; }

        public BigDecimal getRisqueFinancier() { return risqueFinancier; }
        public void setRisqueFinancier(BigDecimal risqueFinancier) { this.risqueFinancier = risqueFinancier; }

        public String getEtapeCourante() { return etapeCourante; }
        public void setEtapeCourante(String etapeCourante) { this.etapeCourante = etapeCourante; }
    }
}
