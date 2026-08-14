package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.JuristeDTO;
import com.carfo.contentieux.model.Juriste;
import com.carfo.contentieux.service.JuristeService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Juristes", description = "Juristes affectés aux dossiers")
@RestController
@RequestMapping("/api/juristes")
public class JuristeController {
    private final JuristeService juristeService;
    public JuristeController(JuristeService juristeService) { this.juristeService = juristeService; }

    @Operation(summary = "Récupérer tous les juristes")
    @GetMapping public List<Juriste> getAll() { return juristeService.getAllJuristes(); }

    @Operation(summary = "Récupérer un juriste par son matricule")
    @GetMapping("/{matricule}") public ResponseEntity<Juriste> getById(@PathVariable String matricule) { return ResponseEntity.of(juristeService.getJuristeById(matricule)); }

    @Operation(summary = "Créer un nouveau juriste")
    @PostMapping public ResponseEntity<Juriste> create(@Valid @RequestBody JuristeDTO dto) {
        Juriste j = new Juriste();
        j.setMatricule(dto.getMatricule());
        j.setNom(dto.getNom());
        j.setPrenoms(dto.getPrenoms());
        j.setSpecialite(dto.getSpecialite());
        return ResponseEntity.status(HttpStatus.CREATED).body(juristeService.createJuriste(j));
    }

    @Operation(summary = "Modifier un juriste existant")
    @PutMapping("/{matricule}") public ResponseEntity<Juriste> update(@PathVariable String matricule, @RequestBody JuristeDTO dto) {
        Juriste j = new Juriste();
        j.setNom(dto.getNom());
        j.setPrenoms(dto.getPrenoms());
        j.setSpecialite(dto.getSpecialite());
        return ResponseEntity.ok(juristeService.updateJuriste(matricule, j));
    }

    @Operation(summary = "Supprimer un juriste")
    @DeleteMapping("/{matricule}") public ResponseEntity<Void> delete(@PathVariable String matricule) { juristeService.deleteJuriste(matricule); return ResponseEntity.noContent().build(); }
}
