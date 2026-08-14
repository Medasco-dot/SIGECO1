package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.Implication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ImplicationDTO {

    @NotBlank
    private String numeroDossier;

    @NotNull
    private Integer idPartie;

    @NotNull
    private Implication.Role role;

    private Implication.LienParente lienParente;

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public Integer getIdPartie() { return idPartie; }
    public void setIdPartie(Integer idPartie) { this.idPartie = idPartie; }

    public Implication.Role getRole() { return role; }
    public void setRole(Implication.Role role) { this.role = role; }

    public Implication.LienParente getLienParente() { return lienParente; }
    public void setLienParente(Implication.LienParente lienParente) { this.lienParente = lienParente; }
}
