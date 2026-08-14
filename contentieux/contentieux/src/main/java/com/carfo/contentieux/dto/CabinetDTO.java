package com.carfo.contentieux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CabinetDTO {

    @NotBlank
    @Size(max = 15)
    private String identifiantCabinet;

    @NotBlank
    @Size(max = 100)
    private String nomCabinet;

    @Size(max = 100)
    private String mail;

    @Size(max = 20)
    private String telephone;

    @Size(max = 100)
    private String adresse;

    public String getIdentifiantCabinet() { return identifiantCabinet; }
    public void setIdentifiantCabinet(String identifiantCabinet) { this.identifiantCabinet = identifiantCabinet; }

    public String getNomCabinet() { return nomCabinet; }
    public void setNomCabinet(String nomCabinet) { this.nomCabinet = nomCabinet; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}
