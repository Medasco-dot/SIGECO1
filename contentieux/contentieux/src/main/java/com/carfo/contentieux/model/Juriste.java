package com.carfo.contentieux.model;

import jakarta.persistence.*;

@Entity
@Table(name = "JURISTE")
public class Juriste {

    @Id
    @Column(length = 15)
    private String matricule;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenoms;

    @Enumerated(EnumType.STRING)
    private Specialite specialite;

    public enum Specialite {
        pension_retraite, pension_reversement, acte_carriere,
        marche_public, penal, polyvalent
    }

    // Getters et setters
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenoms() { return prenoms; }
    public void setPrenoms(String prenoms) { this.prenoms = prenoms; }

    public Specialite getSpecialite() { return specialite; }
    public void setSpecialite(Specialite specialite) { this.specialite = specialite; }
}