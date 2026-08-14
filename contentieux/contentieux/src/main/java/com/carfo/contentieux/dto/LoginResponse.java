package com.carfo.contentieux.dto;

public class LoginResponse {
    private String token;
    private String identifiant;
    private String role;
    private String matriculeJuriste;

    public LoginResponse(String token, String identifiant, String role, String matriculeJuriste) {
        this.token = token;
        this.identifiant = identifiant;
        this.role = role;
        this.matriculeJuriste = matriculeJuriste;
    }

    public String getToken() {
        return token;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public String getRole() {
        return role;
    }

    public String getMatriculeJuriste() {
        return matriculeJuriste;
    }
}
