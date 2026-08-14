package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.UtilisateurDTO;
import com.carfo.contentieux.model.Utilisateur;
import com.carfo.contentieux.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs et rôles")
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {
    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Operation(summary = "Récupérer tous les utilisateurs")
    @GetMapping
    public List<Utilisateur> getAll() {
        return utilisateurService.getAllUtilisateurs();
    }

    @Operation(summary = "Récupérer un utilisateur par son identifiant")
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getById(@PathVariable Integer id) {
        return ResponseEntity.of(utilisateurService.getUtilisateurById(id));
    }

    @Operation(summary = "Créer un nouvel utilisateur")
    @PostMapping
    public ResponseEntity<Utilisateur> create(@Valid @RequestBody UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdentifiant(dto.getIdentifiant());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setRole(dto.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(utilisateurService.createUtilisateur(utilisateur, dto.getMatriculeJuriste()));
    }

    @Operation(summary = "Modifier un utilisateur")
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> update(@PathVariable Integer id, @Valid @RequestBody UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdentifiant(dto.getIdentifiant());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setRole(dto.getRole());
        return ResponseEntity.ok(utilisateurService.updateUtilisateur(id, utilisateur, dto.getMatriculeJuriste()));
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }
}
