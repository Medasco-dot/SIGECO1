package com.carfo.contentieux.model;

import com.carfo.contentieux.util.ChiffrementCnibConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "PARTIE")
public class Partie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Convert(converter = ChiffrementCnibConverter.class)
    @Column(name = "numero_cnib", length = 255)
    private String numeroCnib;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_matrimonial")
    private StatutMatrimonial statutMatrimonial;

    public enum StatutMatrimonial {
        marie, celibataire, divorce, veuf
    }

    // Getters et setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNumeroCnib() { return numeroCnib; }
    public void setNumeroCnib(String numeroCnib) { this.numeroCnib = numeroCnib; }

    public StatutMatrimonial getStatutMatrimonial() { return statutMatrimonial; }
    public void setStatutMatrimonial(StatutMatrimonial statutMatrimonial) { this.statutMatrimonial = statutMatrimonial; }
}