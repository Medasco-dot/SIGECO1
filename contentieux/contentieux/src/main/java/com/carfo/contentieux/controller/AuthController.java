package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.LoginRequest;
import com.carfo.contentieux.dto.LoginResponse;
import com.carfo.contentieux.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentification", description = "Connexion et génération de token")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Operation(summary = "Se connecter")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(utilisateurService.authenticate(request.getIdentifiant(), request.getMotDePasse()));
    }
}
