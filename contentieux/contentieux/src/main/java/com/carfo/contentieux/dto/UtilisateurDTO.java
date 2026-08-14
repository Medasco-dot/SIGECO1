package com.carfo.contentieux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.carfo.contentieux.model.Role;

public class UtilisateurDTO {

    @NotBlank
    @Size(max = 50)
    private String identifiant;

    @NotBlank
    @Size(max = 255)
    private String motDePasse;

    @NotBlank
    @Size(max = 50)
    private String nom;

    @NotBlank
    @Size(max = 50)
    private String prenom;

    private Role role;

    @Size(max = 15)
    private String matriculeJuriste;

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMatriculeJuriste() {
        return matriculeJuriste;
    }

    public void setMatriculeJuriste(String matriculeJuriste) {
        this.matriculeJuriste = matriculeJuriste;
    }
}
