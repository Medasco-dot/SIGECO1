package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.DossierCreateDTO;
import com.carfo.contentieux.dto.DossierDTO;
import com.carfo.contentieux.dto.DossierSearchCriteria;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.repository.TypeContentieuxRepository;
import com.carfo.contentieux.service.DossierExportService;
import com.carfo.contentieux.service.DossierService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Dossiers", description = "Gestion CRUD et recherche des dossiers contentieux")
@RestController
@RequestMapping("/api/dossiers")
public class DossierController {
    private final DossierService dossierService;
    private final TypeContentieuxRepository typeContentieuxRepository;
    private final DossierExportService dossierExportService;

    public DossierController(DossierService dossierService,
                             TypeContentieuxRepository typeContentieuxRepository,
                             DossierExportService dossierExportService) {
        this.dossierService = dossierService;
        this.typeContentieuxRepository = typeContentieuxRepository;
        this.dossierExportService = dossierExportService;
    }

    @Operation(summary = "Récupérer tous les dossiers")
    @GetMapping public List<Dossier> getAll() { return dossierService.getAllDossiers(); }

    // existing search/update/create/delete endpoints kept unchanged
    @Operation(summary = "Rechercher des dossiers par numéro")
    @GetMapping("/recherche/numero") public List<Dossier> rechercherParNumero(@RequestParam String valeur) { return dossierService.rechercherParNumero(valeur); }

    @Operation(summary = "Rechercher des dossiers par type de contentieux")
    @GetMapping("/recherche/type") public List<Dossier> rechercherParType(@RequestParam TypeContentieux.Nature nature) { return dossierService.rechercherParType(nature); }

    @Operation(summary = "Rechercher des dossiers par nom de partie")
    @GetMapping("/recherche/partie") public List<Dossier> rechercherParPartie(@RequestParam String nom) { return dossierService.rechercherParPartie(nom); }

    @Operation(summary = "Rechercher des dossiers par statut/étape")
    @GetMapping("/recherche/statut") public List<Dossier> rechercherParStatut(@RequestParam EtapeDossier.Etape statut) { return dossierService.rechercherParStatut(statut); }

    @Operation(summary = "Recherche multicritère paginée des dossiers")
    @GetMapping("/recherche")
    public Page<Dossier> rechercherMulticritere(
            @RequestParam(required = false) String numeroDossier,
            @RequestParam(required = false) TypeContentieux.Nature typeContentieux,
            @RequestParam(required = false) EtapeDossier.Etape etapeCourante,
            @RequestParam(required = false) String nomPartie,
            @RequestParam(required = false) LocalDate dateOuvertureMin,
            @RequestParam(required = false) LocalDate dateOuvertureMax,
            @RequestParam(required = false) BigDecimal risqueFinancierMin,
            @RequestParam(required = false) BigDecimal risqueFinancierMax,
            @RequestParam(required = false) String matriculeJuriste,
            @RequestParam(required = false) String identifiantCabinet,
            @PageableDefault(size = 20, sort = "numeroDossier", direction = Sort.Direction.ASC) Pageable pageable) {
        DossierSearchCriteria criteria = new DossierSearchCriteria();
        criteria.setNumeroDossier(numeroDossier);
        criteria.setTypeContentieux(typeContentieux);
        criteria.setEtapeCourante(etapeCourante);
        criteria.setNomPartie(nomPartie);
        criteria.setDateOuvertureMin(dateOuvertureMin);
        criteria.setDateOuvertureMax(dateOuvertureMax);
        criteria.setRisqueFinancierMin(risqueFinancierMin);
        criteria.setRisqueFinancierMax(risqueFinancierMax);
        criteria.setMatriculeJuriste(matriculeJuriste);
        criteria.setIdentifiantCabinet(identifiantCabinet);
        return dossierService.rechercherMulticritere(criteria, pageable);
    }

    @Operation(summary = "Récupérer un dossier par son numéro")
    @GetMapping("/{numeroDossier}") public ResponseEntity<Dossier> getById(@PathVariable String numeroDossier) { return ResponseEntity.of(dossierService.getDossierById(numeroDossier)); }

    @Operation(summary = "Créer un nouveau dossier", description = "A la création, le champ numeroDossier ne doit pas être fourni par le client : il est généré par le serveur.")
    @PostMapping public ResponseEntity<Dossier> create(@Valid @RequestBody DossierCreateDTO dto) {
        if (dto.getNumeroDossier() != null && !dto.getNumeroDossier().isBlank()) {
            throw new IllegalArgumentException("Le champ numeroDossier ne doit pas être fourni lors de la création d'un dossier.");
        }
        Dossier d = new Dossier();
        d.setDateOuverture(dto.getDateOuverture());
        d.setResumeAffaire(dto.getResumeAffaire());
        d.setObservation(dto.getObservation());
        d.setRisqueFinancier(dto.getRisqueFinancier());
        d.setMontantReclame(dto.getMontantReclame());
        d.setFraisJustice(dto.getFraisJustice());
        TypeContentieux tc = resolveTypeContentieux(dto);
        if (tc == null) {
            throw new ResourceNotFoundException("Type de contentieux introuvable ou non spécifié");
        }
        d.setTypeContentieux(tc);
        return ResponseEntity.status(HttpStatus.CREATED).body(dossierService.createDossier(d));
    }

    @Operation(summary = "Modifier un dossier existant")
    @PutMapping("/{numeroDossier}") public ResponseEntity<Dossier> update(@PathVariable String numeroDossier,
                                                                            @RequestBody DossierDTO dto) {
        Dossier d = new Dossier();
        d.setDateOuverture(dto.getDateOuverture());
        d.setResumeAffaire(dto.getResumeAffaire());
        d.setObservation(dto.getObservation());
        d.setRisqueFinancier(dto.getRisqueFinancier());
        d.setMontantReclame(dto.getMontantReclame());
        d.setFraisJustice(dto.getFraisJustice());
        TypeContentieux tc = resolveTypeContentieux(dto);
        if (tc != null) {
            d.setTypeContentieux(tc);
        }
        return ResponseEntity.ok(dossierService.updateDossier(numeroDossier, d));
    }

    private TypeContentieux resolveTypeContentieux(DossierCreateDTO dto) {
        if (dto.getNumContentieux() != null) {
            return typeContentieuxRepository.findById(dto.getNumContentieux()).orElse(null);
        }
        if (dto.getNature() != null) {
            return typeContentieuxRepository.findByNature(dto.getNature()).orElse(null);
        }
        return null;
    }

    private TypeContentieux resolveTypeContentieux(DossierDTO dto) {
        if (dto.getNumContentieux() != null) {
            return typeContentieuxRepository.findById(dto.getNumContentieux()).orElse(null);
        }
        if (dto.getNature() != null) {
            return typeContentieuxRepository.findByNature(dto.getNature()).orElse(null);
        }
        return null;
    }

    @Operation(summary = "Supprimer un dossier")
    @DeleteMapping("/{numeroDossier}") public ResponseEntity<Void> delete(@PathVariable String numeroDossier) { dossierService.deleteDossier(numeroDossier); return ResponseEntity.noContent().build(); }

    // ------------------ Export endpoints ------------------
    // La mise en forme des documents (Word/Excel/PDF) vit dans DossierExportService : ces
    // endpoints se contentent de déléguer et d'attacher les en-têtes HTTP appropriés.

    @Operation(summary = "Exporter un dossier au format Word")
    @GetMapping("/{numeroDossier}/export/word")
    public ResponseEntity<byte[]> exportDossierWord(@PathVariable String numeroDossier) throws Exception {
        byte[] contenu = dossierExportService.exportWord(numeroDossier);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fiche-dossier-" + numeroDossier + ".docx");
        return new ResponseEntity<>(contenu, headers, HttpStatus.OK);
    }

    @Operation(summary = "Exporter un dossier au format Excel")
    @GetMapping("/{numeroDossier}/export/excel")
    public ResponseEntity<byte[]> exportDossierExcel(@PathVariable String numeroDossier) throws Exception {
        byte[] contenu = dossierExportService.exportExcel(numeroDossier);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fiche-dossier-" + numeroDossier + ".xlsx");
        return new ResponseEntity<>(contenu, headers, HttpStatus.OK);
    }

    @Operation(summary = "Exporter un dossier au format PDF")
    @GetMapping("/{numeroDossier}/export/pdf")
    public ResponseEntity<byte[]> exportDossierPdf(@PathVariable String numeroDossier) throws Exception {
        byte[] contenu = dossierExportService.exportPdf(numeroDossier);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fiche-dossier-" + numeroDossier + ".pdf");
        return new ResponseEntity<>(contenu, headers, HttpStatus.OK);
    }
}
