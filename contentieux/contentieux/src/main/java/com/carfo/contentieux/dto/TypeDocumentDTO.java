package com.carfo.contentieux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TypeDocumentDTO {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Le code ne doit contenir que des minuscules, chiffres et underscores")
    private String code;

    @NotBlank
    @Size(max = 150)
    private String libelle;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
