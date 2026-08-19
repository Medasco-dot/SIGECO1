package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.UtilisateurDTO;
import com.carfo.contentieux.dto.UtilisateurResponseDTO;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Utilisateur;
import com.carfo.contentieux.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<UtilisateurResponseDTO> getAll() {
        return utilisateurService.getAllUtilisateurs().stream()
                .map(UtilisateurResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Récupérer un utilisateur par son identifiant")
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDTO> getById(@PathVariable Integer id) {
        return utilisateurService.getUtilisateurById(id)
                .map(UtilisateurResponseDTO::new)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id " + id));
    }

    @Operation(summary = "Créer un nouvel utilisateur")
    @PostMapping
    public ResponseEntity<UtilisateurResponseDTO> create(@Valid @RequestBody UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdentifiant(dto.getIdentifiant());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setRole(dto.getRole());
        Utilisateur cree = utilisateurService.createUtilisateur(utilisateur, dto.getMatriculeJuriste());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UtilisateurResponseDTO(cree));
    }

    @Operation(summary = "Modifier un utilisateur")
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdentifiant(dto.getIdentifiant());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setRole(dto.getRole());
        Utilisateur modifie = utilisateurService.updateUtilisateur(id, utilisateur, dto.getMatriculeJuriste());
        return ResponseEntity.ok(new UtilisateurResponseDTO(modifie));
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }
}
