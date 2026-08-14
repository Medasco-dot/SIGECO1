package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.DossierJuristeDTO;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.DossierJuriste;
import com.carfo.contentieux.model.DossierJuristeId;
import com.carfo.contentieux.model.Juriste;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.JuristeRepository;
import com.carfo.contentieux.service.DossierJuristeService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Affectations juristes")
@RestController
@RequestMapping("/api/dossiers-juristes")
public class DossierJuristeController {
    private final DossierJuristeService dossierJuristeService;
    private final DossierRepository dossierRepository;
    private final JuristeRepository juristeRepository;

    public DossierJuristeController(DossierJuristeService dossierJuristeService,
                                     DossierRepository dossierRepository,
                                     JuristeRepository juristeRepository) {
        this.dossierJuristeService = dossierJuristeService;
        this.dossierRepository = dossierRepository;
        this.juristeRepository = juristeRepository;
    }

    @Operation(summary = "Récupérer toutes les affectations juristes")
    @GetMapping public List<DossierJuriste> getAll() { return dossierJuristeService.getAllDossierJuristes(); }

    @Operation(summary = "Récupérer les affectations juristes d'un dossier")
    @GetMapping("/dossier/{numeroDossier}")
    public List<DossierJuriste> getByDossier(@PathVariable String numeroDossier) {
        return dossierJuristeService.getDossierJuristesByDossier(numeroDossier);
    }

    @Operation(summary = "Récupérer une affectation juriste par son identifiant composite")
    @GetMapping("/{numeroDossier}/{matricule}")
    public ResponseEntity<DossierJuriste> getById(@PathVariable String numeroDossier, @PathVariable String matricule) {
        return ResponseEntity.of(dossierJuristeService.getDossierJuristeById(new DossierJuristeId(numeroDossier, matricule)));
    }

    @Operation(summary = "Créer une nouvelle affectation juriste")
    @PostMapping public ResponseEntity<DossierJuriste> create(@Valid @RequestBody DossierJuristeDTO dto) {
        Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
        Juriste juriste = juristeRepository.findById(dto.getMatricule())
                .orElseThrow(() -> new ResourceNotFoundException("Juriste introuvable avec le matricule " + dto.getMatricule()));
        DossierJuriste dj = new DossierJuriste();
        dj.setId(new DossierJuristeId(dto.getNumeroDossier(), dto.getMatricule()));
        dj.setDossier(dossier);
        dj.setJuriste(juriste);
        return ResponseEntity.status(HttpStatus.CREATED).body(dossierJuristeService.createDossierJuriste(dj));
    }

    @Operation(summary = "Modifier une affectation juriste existante")
    @PutMapping("/{numeroDossier}/{matricule}")
    public ResponseEntity<DossierJuriste> update(@PathVariable String numeroDossier,
                                                  @PathVariable String matricule,
                                                  @RequestBody DossierJuristeDTO dto) {
        DossierJuriste dj = new DossierJuriste();
        if (dto.getNumeroDossier() != null && !dto.getNumeroDossier().equals(numeroDossier)) {
            dj.setDossier(dossierRepository.findById(dto.getNumeroDossier())
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier())));
        }
        if (dto.getMatricule() != null && !dto.getMatricule().equals(matricule)) {
            dj.setJuriste(juristeRepository.findById(dto.getMatricule())
                    .orElseThrow(() -> new ResourceNotFoundException("Juriste introuvable avec le matricule " + dto.getMatricule())));
        }
        return ResponseEntity.ok(dossierJuristeService.updateDossierJuriste(
                new DossierJuristeId(numeroDossier, matricule), dj));
    }

    @Operation(summary = "Supprimer une affectation juriste")
    @DeleteMapping("/{numeroDossier}/{matricule}")
    public ResponseEntity<Void> delete(@PathVariable String numeroDossier, @PathVariable String matricule) {
        dossierJuristeService.deleteDossierJuriste(new DossierJuristeId(numeroDossier, matricule));
        return ResponseEntity.noContent().build();
    }
}
