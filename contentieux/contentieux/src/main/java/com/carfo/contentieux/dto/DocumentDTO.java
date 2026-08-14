package com.carfo.contentieux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class DocumentDTO {

    private Integer idDocument;

    private String typeDocument;

    private LocalDate dateAjout;

    @Size(max = 255)
    private String fichier;

    @NotBlank
    @Size(max = 50)
    private String numeroDossier;

    public Integer getIdDocument() { return idDocument; }
    public void setIdDocument(Integer idDocument) { this.idDocument = idDocument; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public LocalDate getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDate dateAjout) { this.dateAjout = dateAjout; }

    public String getFichier() { return fichier; }
    public void setFichier(String fichier) { this.fichier = fichier; }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }
}
