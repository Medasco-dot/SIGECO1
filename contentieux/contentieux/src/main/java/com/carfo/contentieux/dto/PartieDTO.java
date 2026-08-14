package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.Partie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PartieDTO {

    private Integer id;

    @NotBlank
    @Size(max = 50)
    private String nom;

    @NotBlank
    @Size(max = 50)
    private String prenom;

    @Size(max = 20)
    private String numeroCnib;

    private Partie.StatutMatrimonial statutMatrimonial;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNumeroCnib() { return numeroCnib; }
    public void setNumeroCnib(String numeroCnib) { this.numeroCnib = numeroCnib; }

    public Partie.StatutMatrimonial getStatutMatrimonial() { return statutMatrimonial; }
    public void setStatutMatrimonial(Partie.StatutMatrimonial statutMatrimonial) { this.statutMatrimonial = statutMatrimonial; }
}
