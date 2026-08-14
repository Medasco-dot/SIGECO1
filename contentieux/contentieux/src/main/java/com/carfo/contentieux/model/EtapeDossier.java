package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ETAPE_DOSSIER")
public class EtapeDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Etape etape;

    public enum Etape{
        ouvert,
        en_instruction,
        juge,
        en_appel,
        en_cassation,
        cloture,
        classe_sans_suite
    }

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @ManyToOne
    @JoinColumn(name = "numero_dossier", nullable = false)
    @JsonIgnore
    private Dossier dossier;

    @JsonProperty("numeroDossier")
    @Transient
    public String getNumeroDossier() {
        return dossier != null ? dossier.getNumeroDossier() : null;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Etape getEtape() { return etape; }
    public void setEtape(Etape etape) { this.etape = etape; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    @JsonIgnore
    public Dossier getDossier() { return dossier; }
    public void setDossier(Dossier dossier) { this.dossier = dossier; }

}
