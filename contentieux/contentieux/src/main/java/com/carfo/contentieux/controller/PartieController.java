package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.PartieDTO;
import com.carfo.contentieux.model.Partie;
import com.carfo.contentieux.service.PartieService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parties", description = "Personnes physiques impliquées dans les dossiers")
@RestController
@RequestMapping("/api/parties")
public class PartieController {
    private final PartieService partieService;
    public PartieController(PartieService partieService) { this.partieService = partieService; }

    @Operation(summary = "Récupérer toutes les parties")
    @GetMapping public List<Partie> getAll() { return partieService.getAllParties(); }

    @Operation(summary = "Récupérer une partie par son ID")
    @GetMapping("/{id}") public ResponseEntity<Partie> getById(@PathVariable Integer id) { return ResponseEntity.of(partieService.getPartieById(id)); }

    @Operation(summary = "Créer une nouvelle partie")
    @PostMapping public ResponseEntity<Partie> create(@Valid @RequestBody PartieDTO dto) {
        Partie p = new Partie();
        p.setNom(dto.getNom());
        p.setPrenom(dto.getPrenom());
        p.setNumeroCnib(dto.getNumeroCnib());
        p.setStatutMatrimonial(dto.getStatutMatrimonial());
        return ResponseEntity.status(HttpStatus.CREATED).body(partieService.createPartie(p));
    }

    @Operation(summary = "Modifier une partie existante")
    @PutMapping("/{id}") public ResponseEntity<Partie> update(@PathVariable Integer id, @Valid @RequestBody PartieDTO dto) {
        Partie p = new Partie();
        p.setNom(dto.getNom());
        p.setPrenom(dto.getPrenom());
        p.setNumeroCnib(dto.getNumeroCnib());
        p.setStatutMatrimonial(dto.getStatutMatrimonial());
        return ResponseEntity.ok(partieService.updatePartie(id, p));
    }

    @Operation(summary = "Supprimer une partie")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { partieService.deletePartie(id); return ResponseEntity.noContent().build(); }
}
