package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.TypeDocumentDTO;
import com.carfo.contentieux.model.TypeDocument;
import com.carfo.contentieux.service.TypeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Document types", description = "Référentiel des types de documents pouvant être associés à un dossier")
@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeController {

    private final TypeDocumentService typeDocumentService;

    public DocumentTypeController(TypeDocumentService typeDocumentService) {
        this.typeDocumentService = typeDocumentService;
    }

    @Operation(summary = "Récupérer la liste des types de document")
    @GetMapping
    public List<TypeDocument> getAll() {
        return typeDocumentService.getAll();
    }

    @Operation(summary = "Créer un nouveau type de document")
    @PostMapping
    public ResponseEntity<TypeDocument> create(@Valid @RequestBody TypeDocumentDTO dto) {
        TypeDocument t = new TypeDocument();
        t.setCode(dto.getCode());
        t.setLibelle(dto.getLibelle());
        return ResponseEntity.status(HttpStatus.CREATED).body(typeDocumentService.create(t));
    }

    @Operation(summary = "Modifier le libellé d'un type de document existant")
    @PutMapping("/{code}")
    public ResponseEntity<TypeDocument> update(@PathVariable String code, @Valid @RequestBody TypeDocumentDTO dto) {
        TypeDocument t = new TypeDocument();
        t.setLibelle(dto.getLibelle());
        return ResponseEntity.ok(typeDocumentService.update(code, t));
    }

    @Operation(summary = "Supprimer un type de document (refusé s'il est déjà utilisé par un document)")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        typeDocumentService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
