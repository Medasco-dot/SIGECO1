package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.AudienceDecisionDTO;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.AudienceDecision;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.service.AudienceDecisionService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Audiences & décisions")
@RestController
@RequestMapping("/api/audiences-decisions")
public class AudienceDecisionController {
    private final AudienceDecisionService audienceDecisionService;
    private final DossierRepository dossierRepository;

    public AudienceDecisionController(AudienceDecisionService audienceDecisionService,
                                       DossierRepository dossierRepository) {
        this.audienceDecisionService = audienceDecisionService;
        this.dossierRepository = dossierRepository;
    }

    @Operation(summary = "Récupérer toutes les audiences et décisions")
    @GetMapping public List<AudienceDecision> getAll() { return audienceDecisionService.getAllAudienceDecisions(); }

    @Operation(summary = "Rechercher des audiences par type d'étape")
    @GetMapping("/recherche/type")
    public List<AudienceDecision> rechercherParType(@RequestParam AudienceDecision.TypeEtape type) {
        return audienceDecisionService.findByTypeEtape(type);
    }

    @Operation(summary = "Rechercher des audiences par période")
    @GetMapping("/recherche/date")
    public List<AudienceDecision> rechercherParDate(@RequestParam java.time.LocalDate debut,
                                                     @RequestParam java.time.LocalDate fin) {
        return audienceDecisionService.findByDateBetween(debut, fin);
    }

    @Operation(summary = "Rechercher des audiences par issue pour CARFO")
    @GetMapping("/recherche/issue")
    public List<AudienceDecision> rechercherParIssue(@RequestParam AudienceDecision.IssuePourCarfo issue) {
        return audienceDecisionService.findByIssue(issue);
    }

    @Operation(summary = "Récupérer les audiences/décisions d'un dossier")
    @GetMapping("/dossier/{numeroDossier}") public List<AudienceDecision> getByDossier(@PathVariable String numeroDossier) {
        return audienceDecisionService.getAudienceDecisionsByDossier(numeroDossier);
    }

    @Operation(summary = "Récupérer une audience/décision par son ID")
    @GetMapping("/{id}") public ResponseEntity<AudienceDecision> getById(@PathVariable Integer id) { return ResponseEntity.of(audienceDecisionService.getAudienceDecisionById(id)); }

    @Operation(summary = "Créer une nouvelle audience/décision")
    @PostMapping public ResponseEntity<AudienceDecision> create(@Valid @RequestBody AudienceDecisionDTO dto) {
        AudienceDecision ad = new AudienceDecision();
        ad.setDate(dto.getDate());
        ad.setLieuAudience(dto.getLieuAudience());
        ad.setTypeEtape(dto.getTypeEtape());
        ad.setNatureDecision(dto.getNatureDecision());
        ad.setResumeDecision(dto.getResumeDecision());
        ad.setIssuePourCarfo(dto.getIssuePourCarfo());
        ad.setMontantObtenu(dto.getMontantObtenu());
        ad.setMontantDu(dto.getMontantDu());
        ad.setFraisJustice(dto.getFraisJustice());
        Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
        ad.setDossier(dossier);
        return ResponseEntity.status(HttpStatus.CREATED).body(audienceDecisionService.createAudienceDecision(ad));
    }

    @Operation(summary = "Modifier une audience/décision existante")
    @PutMapping("/{id}") public ResponseEntity<AudienceDecision> update(@PathVariable Integer id,
                                                                         @RequestBody AudienceDecisionDTO dto) {
        AudienceDecision ad = new AudienceDecision();
        ad.setDate(dto.getDate());
        ad.setLieuAudience(dto.getLieuAudience());
        ad.setTypeEtape(dto.getTypeEtape());
        ad.setNatureDecision(dto.getNatureDecision());
        ad.setResumeDecision(dto.getResumeDecision());
        ad.setIssuePourCarfo(dto.getIssuePourCarfo());
        ad.setMontantObtenu(dto.getMontantObtenu());
        ad.setMontantDu(dto.getMontantDu());
        ad.setFraisJustice(dto.getFraisJustice());
        if (dto.getNumeroDossier() != null) {
            Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
            ad.setDossier(dossier);
        }
        return ResponseEntity.ok(audienceDecisionService.updateAudienceDecision(id, ad));
    }

    @Operation(summary = "Supprimer une audience/décision")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { audienceDecisionService.deleteAudienceDecision(id); return ResponseEntity.noContent().build(); }
}
