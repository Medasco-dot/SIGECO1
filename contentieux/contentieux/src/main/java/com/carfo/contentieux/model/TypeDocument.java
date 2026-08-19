package com.carfo.contentieux.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TYPE_DOCUMENT")
public class TypeDocument {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "libelle", nullable = false, length = 150)
    private String libelle;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
