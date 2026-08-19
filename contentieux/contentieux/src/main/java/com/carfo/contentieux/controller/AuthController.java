package com.carfo.contentieux.controller;

import com.carfo.contentieux.config.SecurityConfig;
import com.carfo.contentieux.dto.LoginRequest;
import com.carfo.contentieux.dto.LoginResponse;
import com.carfo.contentieux.model.RevokedToken;
import com.carfo.contentieux.repository.RevokedTokenRepository;
import com.carfo.contentieux.service.JwtService;
import com.carfo.contentieux.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentification", description = "Connexion, deconnexion et generation de token")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UtilisateurService utilisateurService;
    private final RevokedTokenRepository revokedTokenRepository;

    public AuthController(UtilisateurService utilisateurService, RevokedTokenRepository revokedTokenRepository) {
        this.utilisateurService = utilisateurService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Operation(summary = "Se connecter")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(utilisateurService.authenticate(request.getIdentifiant(), request.getMotDePasse()));
    }

    @Operation(summary = "Se déconnecter : révoque immédiatement le jeton en cours (sans attendre son expiration)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        Object attribut = request.getAttribute(SecurityConfig.JwtAuthenticationFilter.CLAIMS_REQUEST_ATTRIBUTE);
        if (attribut instanceof JwtService.JwtClaims claims) {
            revokedTokenRepository.save(new RevokedToken(claims.getJti(), claims.getExpiration()));
        }
        return ResponseEntity.noContent().build();
    }
}
