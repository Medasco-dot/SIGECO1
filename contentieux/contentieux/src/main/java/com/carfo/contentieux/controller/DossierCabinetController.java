package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.DossierCabinetDTO;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Cabinet;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.DossierCabinet;
import com.carfo.contentieux.model.DossierCabinetId;
import com.carfo.contentieux.repository.CabinetRepository;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.service.DossierCabinetService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Affectations cabinets")
@RestController
@RequestMapping("/api/dossiers-cabinets")
public class DossierCabinetController {
    private final DossierCabinetService dossierCabinetService;
    private final DossierRepository dossierRepository;
    private final CabinetRepository cabinetRepository;

    public DossierCabinetController(DossierCabinetService dossierCabinetService,
                                     DossierRepository dossierRepository,
                                     CabinetRepository cabinetRepository) {
        this.dossierCabinetService = dossierCabinetService;
        this.dossierRepository = dossierRepository;
        this.cabinetRepository = cabinetRepository;
    }

    @Operation(summary = "Récupérer toutes les affectations cabinets")
    @GetMapping public List<DossierCabinet> getAll() { return dossierCabinetService.getAllDossierCabinets(); }

    @Operation(summary = "Récupérer les affectations cabinets d'un dossier")
    @GetMapping("/dossier/{numeroDossier}")
    public List<DossierCabinet> getByDossier(@PathVariable String numeroDossier) {
        return dossierCabinetService.getDossierCabinetsByDossier(numeroDossier);
    }

    @Operation(summary = "Récupérer une affectation cabinet par son identifiant composite")
    @GetMapping("/{numeroDossier}/{identifiantCabinet}")
    public ResponseEntity<DossierCabinet> getById(@PathVariable String numeroDossier,
                                                   @PathVariable String identifiantCabinet) {
        return ResponseEntity.of(dossierCabinetService.getDossierCabinetById(
                new DossierCabinetId(numeroDossier, identifiantCabinet)));
    }

    @Operation(summary = "Créer une nouvelle affectation cabinet")
    @PostMapping public ResponseEntity<DossierCabinet> create(@Valid @RequestBody DossierCabinetDTO dto) {
        Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
        Cabinet cabinet = cabinetRepository.findById(dto.getIdentifiantCabinet())
                .orElseThrow(() -> new ResourceNotFoundException("Cabinet introuvable avec l'identifiant " + dto.getIdentifiantCabinet()));
        DossierCabinet dc = new DossierCabinet();
        dc.setId(new DossierCabinetId(dto.getNumeroDossier(), dto.getIdentifiantCabinet()));
        dc.setDossier(dossier);
        dc.setCabinet(cabinet);
        dc.setNomAvocatReferent(dto.getNomAvocatReferent());
        return ResponseEntity.status(HttpStatus.CREATED).body(dossierCabinetService.createDossierCabinet(dc));
    }

    @Operation(summary = "Modifier une affectation cabinet existante")
    @PutMapping("/{numeroDossier}/{identifiantCabinet}")
    public ResponseEntity<DossierCabinet> update(@PathVariable String numeroDossier,
                                                  @PathVariable String identifiantCabinet,
                                                  @RequestBody DossierCabinetDTO dto) {
        DossierCabinet dc = new DossierCabinet();
        dc.setNomAvocatReferent(dto.getNomAvocatReferent());
        if (dto.getNumeroDossier() != null && !dto.getNumeroDossier().equals(numeroDossier)) {
            dc.setDossier(dossierRepository.findById(dto.getNumeroDossier())
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier())));
        }
        if (dto.getIdentifiantCabinet() != null && !dto.getIdentifiantCabinet().equals(identifiantCabinet)) {
            dc.setCabinet(cabinetRepository.findById(dto.getIdentifiantCabinet())
                    .orElseThrow(() -> new ResourceNotFoundException("Cabinet introuvable avec l'identifiant " + dto.getIdentifiantCabinet())));
        }
        return ResponseEntity.ok(dossierCabinetService.updateDossierCabinet(
                new DossierCabinetId(numeroDossier, identifiantCabinet), dc));
    }

    @Operation(summary = "Supprimer une affectation cabinet")
    @DeleteMapping("/{numeroDossier}/{identifiantCabinet}")
    public ResponseEntity<Void> delete(@PathVariable String numeroDossier,
                                        @PathVariable String identifiantCabinet) {
        dossierCabinetService.deleteDossierCabinet(new DossierCabinetId(numeroDossier, identifiantCabinet));
        return ResponseEntity.noContent().build();
    }
}
