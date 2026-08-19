package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "UTILISATEUR")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "identifiant", nullable = false, unique = true, length = 50)
    private String identifiant;

    @NotBlank
    @Size(max = 255)
    @Column(name = "mot_de_passe", nullable = false, length = 255)
    @JsonIgnore
    private String motDePasse;

    @NotBlank
    @Size(max = 50)
    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @NotBlank
    @Size(max = 50)
    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricule_juriste")
    private Juriste juriste;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Juriste getJuriste() {
        return juriste;
    }

    public void setJuriste(Juriste juriste) {
        this.juriste = juriste;
    }
}
