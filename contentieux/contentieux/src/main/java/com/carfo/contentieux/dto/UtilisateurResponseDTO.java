package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.Role;
import com.carfo.contentieux.model.Utilisateur;

/**
 * DTO de sortie pour Utilisateur : ne contient jamais le hash du mot de passe, contrairement
 * à l'entité JPA. Utilisé sur tous les endpoints de lecture pour éviter d'exposer
 * accidentellement {@code motDePasse} dans une réponse JSON.
 */
public class UtilisateurResponseDTO {

    private final Integer id;
    private final String identifiant;
    private final String nom;
    private final String prenom;
    private final Role role;
    private final String matriculeJuriste;

    public UtilisateurResponseDTO(Utilisateur u) {
        this.id = u.getId();
        this.identifiant = u.getIdentifiant();
        this.nom = u.getNom();
        this.prenom = u.getPrenom();
        this.role = u.getRole();
        this.matriculeJuriste = u.getJuriste() != null ? u.getJuriste().getMatricule() : null;
    }

    public Integer getId() { return id; }
    public String getIdentifiant() { return identifiant; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Role getRole() { return role; }
    public String getMatriculeJuriste() { return matriculeJuriste; }
}
