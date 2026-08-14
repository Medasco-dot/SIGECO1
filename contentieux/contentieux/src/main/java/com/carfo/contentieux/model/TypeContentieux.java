package com.carfo.contentieux.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TYPE_CONTENTIEUX")
public class TypeContentieux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_contentieux")
    private Integer numContentieux;

    @Enumerated(EnumType.STRING)
    private Nature nature;

    public enum Nature {
        pension_retraite, pension_reversement, acte_carriere,
        marche_public, penal, autre
    }

    // Getters et setters
    public Integer getNumContentieux() { return numContentieux; }
    public void setNumContentieux(Integer numContentieux) { this.numContentieux = numContentieux; }

    public Nature getNature() { return nature; }
    public void setNature(Nature nature) { this.nature = nature; }
}