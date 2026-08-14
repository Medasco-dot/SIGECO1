package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.DocumentDTO;
import com.carfo.contentieux.exception.InvalidFileException;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Document;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.service.DocumentService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Documents", description = "Gestion des documents liés aux dossiers")
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final DossierRepository dossierRepository;

    public DocumentController(DocumentService documentService,
                              DossierRepository dossierRepository) {
        this.documentService = documentService;
        this.dossierRepository = dossierRepository;
    }

    @Operation(summary = "Récupérer tous les documents")
    @GetMapping public List<Document> getAll() { return documentService.getAllDocuments(); }

    @Operation(summary = "Rechercher des documents par type")
    @GetMapping("/recherche/type")
    public List<Document> rechercherParType(@RequestParam String type) {
        return documentService.findByType(type);
    }

    @Operation(summary = "Rechercher des documents par période d'ajout")
    @GetMapping("/recherche/date")
    public List<Document> rechercherParDate(@RequestParam LocalDate debut,
                                            @RequestParam LocalDate fin) {
        return documentService.findByDateAjoutBetween(debut, fin);
    }

    @Operation(summary = "Récupérer les documents d'un dossier")
    @GetMapping("/dossier/{numeroDossier}") public List<Document> getByDossier(@PathVariable String numeroDossier) {
        return documentService.getDocumentsByDossier(numeroDossier);
    }

    @Operation(summary = "Récupérer un document par son ID")
    @GetMapping("/{id}") public ResponseEntity<Document> getById(@PathVariable Integer id) { return ResponseEntity.of(documentService.getDocumentById(id)); }

    @Operation(summary = "Créer un enregistrement de document (métadonnées)")
    @PostMapping public ResponseEntity<Document> create(@Valid @RequestBody DocumentDTO dto) {
        Document d = new Document();
        d.setTypeDocument(dto.getTypeDocument());
        d.setDateAjout(dto.getDateAjout());
        d.setFichier(dto.getFichier());
        Dossier dossier = dossierRepository.findById(dto.getNumeroDossier())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + dto.getNumeroDossier()));
        d.setDossier(dossier);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createDocument(d));
    }

    @Operation(summary = "Créer un document à partir d'un fichier uploadé")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> upload(@RequestParam("numeroDossier") String numeroDossier,
                                          @RequestParam(value = "typeDocument", required = false) String typeDocument,
                                          @RequestParam(value = "dateAjout", required = false) String dateAjout,
                                          @RequestParam(value = "fichier", required = false) MultipartFile file) throws IOException {
        Document d = new Document();
        d.setTypeDocument(typeDocument);
        if (dateAjout != null && !dateAjout.isBlank()) {
            d.setDateAjout(LocalDate.parse(dateAjout));
        }
        if (file != null && !file.isEmpty()) {
            Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "contentieux", "uploads");
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                    ? "document"
                    : file.getOriginalFilename();
            // Strip any directory components (and Windows-style separators) to prevent path traversal
            String sanitizedName = Paths.get(originalName).getFileName().toString().replace("\\", "");
            if (sanitizedName.isBlank()) {
                sanitizedName = "document";
            }
            String storedName = System.currentTimeMillis() + "-" + sanitizedName;
            Path target = uploadDir.resolve(storedName).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new InvalidFileException("Nom de fichier invalide");
            }
            file.transferTo(target);
            d.setFichier(target.toString());
        }

        Dossier dossier = dossierRepository.findById(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + numeroDossier));
        d.setDossier(dossier);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createDocument(d));
    }

    @Operation(summary = "Modifier les métadonnées d'un document")
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateMetadata(@PathVariable Integer id,
                                                    @RequestBody DocumentDTO dto) {
        Document updated = documentService.updateMetadata(id, dto.getTypeDocument(), dto.getDateAjout(), dto.getFichier(), dto.getNumeroDossier());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer un document")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { documentService.deleteDocument(id); return ResponseEntity.noContent().build(); }
}
