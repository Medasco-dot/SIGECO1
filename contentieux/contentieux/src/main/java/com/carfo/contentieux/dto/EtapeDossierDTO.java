package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.EtapeDossier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class EtapeDossierDTO {

    private Integer id;

    @NotNull
    private EtapeDossier.Etape etape;

    @NotNull
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @NotBlank
    @Size(max = 50)
    private String numeroDossier;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public EtapeDossier.Etape getEtape() { return etape; }
    public void setEtape(EtapeDossier.Etape etape) { this.etape = etape; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }
}
