package com.carfo.contentieux.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "DOCUMENT")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Integer idDocument;

    @Column(name = "type_document", length = 120)
    private String typeDocument;

    @Column(name = "date_ajout")
    private LocalDate dateAjout;

    @Column(length = 255)
    private String fichier;

    @ManyToOne
    @JoinColumn(name = "numero_dossier", nullable = false)
    @JsonIgnore
    private Dossier dossier;

    @JsonProperty("numeroDossier")
    @Transient
    public String getNumeroDossier() {
        return dossier != null ? dossier.getNumeroDossier() : null;
    }

    public Integer getIdDocument() { return idDocument; }
    public void setIdDocument(Integer idDocument) { this.idDocument = idDocument; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public LocalDate getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDate dateAjout) { this.dateAjout = dateAjout; }

    public String getFichier() { return fichier; }
    public void setFichier(String fichier) { this.fichier = fichier; }

    @JsonIgnore
    public Dossier getDossier() { return dossier; }
    public void setDossier(Dossier dossier) { this.dossier = dossier; }
}
