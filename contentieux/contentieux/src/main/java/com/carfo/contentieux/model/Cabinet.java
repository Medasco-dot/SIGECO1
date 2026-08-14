package com.carfo.contentieux.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CABINET")
public class Cabinet {

    @Id
    @Column(name = "identifiant_cabinet" , length = 15)
    private String identifiantCabinet;

    @Column(name = "nom_cabinet" , nullable = false, length = 100)
    private String nomCabinet;

    @Column(length = 100)
    private String mail;

    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String adresse;

    public String getIdentifiantCabinet() { return identifiantCabinet; }
    public void setIdentifiantCabinet(String identifiantCabinet) { this.identifiantCabinet = identifiantCabinet; }

    public String getNomCabinet() { return nomCabinet; }
    public void setNomCabinet(String nomCabinet) { this.nomCabinet = nomCabinet; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String  adresse) { this.adresse = adresse; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
}
