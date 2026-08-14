package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.Juriste;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JuristeDTO {

    @NotBlank
    @Size(max = 15)
    private String matricule;

    @NotBlank
    @Size(max = 50)
    private String nom;

    @NotBlank
    @Size(max = 50)
    private String prenoms;

    @NotNull
    private Juriste.Specialite specialite;

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenoms() { return prenoms; }
    public void setPrenoms(String prenoms) { this.prenoms = prenoms; }

    public Juriste.Specialite getSpecialite() { return specialite; }
    public void setSpecialite(Juriste.Specialite specialite) { this.specialite = specialite; }
}
