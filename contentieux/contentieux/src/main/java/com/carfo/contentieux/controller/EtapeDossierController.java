package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.EtapeDossierDTO;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.service.EtapeDossierService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Étapes dossier")
@RestController
@RequestMapping("/api/etapes-dossier")
public class EtapeDossierController {
    private final EtapeDossierService etapeDossierService;
    private final DossierRepository dossierRepository;

    public EtapeDossierController(EtapeDossierService etapeDossierService,
                                   DossierRepository dossierRepository) {
        this.etapeDossierService = etapeDossierService;
        this.dossierRepository = dossierRepository;
    }

    @Operation(summary = "Récupérer toutes les étapes de dossier")
    @GetMapping public List<EtapeDossier> getAll() { return etapeDossierService.getAllEtapesDossier(); }

    @Operation(summary = "Récupérer les étapes d'un dossier")
    @GetMapping("/dossier/{numeroDossier}")
    public List<EtapeDossier> getByDossier(@PathVariable String numeroDossier) {
        return etapeDossierService.getEtapesByDossier(numeroDossier);
    }

    @Operation(summary = "Récupérer une étape de dossier par son ID")
    @GetMapping("/{id}") public ResponseEntity<EtapeDossier> getById(@PathVariable Integer id) { return ResponseEntity.of(etapeDossierService.getEtapeDossierById(id)); }

    @Operation(summary = "Créer une nouvelle étape de dossier")
    @PostMapping public ResponseEntity<EtapeDossier> create(@Valid @RequestBody EtapeDossierDTO dto) {
        EtapeDossier ed = new EtapeDossier();
        ed.setEtape(dto.getEtape());
        ed.setDateDebut(dto.getDateDebut());
        ed.setDateFin(dto.getDateFin());
        Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
        ed.setDossier(dossier);
        return ResponseEntity.status(HttpStatus.CREATED).body(etapeDossierService.createEtapeDossier(ed));
    }

    @Operation(summary = "Modifier une étape de dossier existante")
    @PutMapping("/{id}") public ResponseEntity<EtapeDossier> update(@PathVariable Integer id,
                                                                     @RequestBody EtapeDossierDTO dto) {
        EtapeDossier ed = new EtapeDossier();
        ed.setEtape(dto.getEtape());
        ed.setDateDebut(dto.getDateDebut());
        ed.setDateFin(dto.getDateFin());
        if (dto.getNumeroDossier() != null) {
            Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
            ed.setDossier(dossier);
        }
        return ResponseEntity.ok(etapeDossierService.updateEtapeDossier(id, ed));
    }

    @Operation(summary = "Supprimer une étape de dossier")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { etapeDossierService.deleteEtapeDossier(id); return ResponseEntity.noContent().build(); }
}
