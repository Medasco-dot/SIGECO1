package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.ImplicationDTO;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Implication;
import com.carfo.contentieux.model.ImplicationId;
import com.carfo.contentieux.model.Partie;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.PartieRepository;
import com.carfo.contentieux.service.ImplicationService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Implications", description = "Lien entre dossier, partie, rôle et lien de parenté")
@RestController
@RequestMapping("/api/implications")
public class ImplicationController {
    private final ImplicationService implicationService;
    private final DossierRepository dossierRepository;
    private final PartieRepository partieRepository;

    public ImplicationController(ImplicationService implicationService,
                                  DossierRepository dossierRepository,
                                  PartieRepository partieRepository) {
        this.implicationService = implicationService;
        this.dossierRepository = dossierRepository;
        this.partieRepository = partieRepository;
    }

    @Operation(summary = "Récupérer toutes les implications")
    @GetMapping public List<Implication> getAll() { return implicationService.getAllImplications(); }

    @Operation(summary = "Récupérer les implications d'un dossier")
    @GetMapping("/dossier/{numeroDossier}")
    public List<Implication> getByDossier(@PathVariable String numeroDossier) {
        return implicationService.getImplicationsByDossier(numeroDossier);
    }

    @Operation(summary = "Récupérer une implication par son identifiant composite")
    @GetMapping("/{numeroDossier}/{idPartie}")
    public ResponseEntity<Implication> getById(@PathVariable String numeroDossier, @PathVariable Integer idPartie) {
        return ResponseEntity.of(implicationService.getImplicationById(new ImplicationId(numeroDossier, idPartie)));
    }

    @Operation(summary = "Créer une nouvelle implication")
    @PostMapping public ResponseEntity<Implication> create(@Valid @RequestBody ImplicationDTO dto) {
        Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
        Partie partie = partieRepository.findById(dto.getIdPartie())
                .orElseThrow(() -> new ResourceNotFoundException("Partie introuvable avec l'id " + dto.getIdPartie()));
        Implication impl = new Implication();
        impl.setId(new ImplicationId(dto.getNumeroDossier(), dto.getIdPartie()));
        impl.setDossier(dossier);
        impl.setPartie(partie);
        impl.setRole(dto.getRole());
        impl.setLienParente(dto.getLienParente());
        return ResponseEntity.status(HttpStatus.CREATED).body(implicationService.createImplication(impl));
    }

    @Operation(summary = "Modifier une implication existante")
    @PutMapping("/{numeroDossier}/{idPartie}")
    public ResponseEntity<Implication> update(@PathVariable String numeroDossier,
                                              @PathVariable Integer idPartie,
                                              @RequestBody ImplicationDTO dto) {
        Implication impl = new Implication();
        impl.setRole(dto.getRole());
        impl.setLienParente(dto.getLienParente());
        if (dto.getNumeroDossier() != null && !dto.getNumeroDossier().equals(numeroDossier)) {
            impl.setDossier(dossierRepository.findById(dto.getNumeroDossier())
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier())));
        }
        if (dto.getIdPartie() != null && !dto.getIdPartie().equals(idPartie)) {
            impl.setPartie(partieRepository.findById(dto.getIdPartie())
                    .orElseThrow(() -> new ResourceNotFoundException("Partie introuvable avec l'id " + dto.getIdPartie())));
        }
        return ResponseEntity.ok(implicationService.updateImplication(new ImplicationId(numeroDossier, idPartie), impl));
    }

    @Operation(summary = "Supprimer une implication")
    @DeleteMapping("/{numeroDossier}/{idPartie}")
    public ResponseEntity<Void> delete(@PathVariable String numeroDossier, @PathVariable Integer idPartie) {
        implicationService.deleteImplication(new ImplicationId(numeroDossier, idPartie));
        return ResponseEntity.noContent().build();
    }
}
