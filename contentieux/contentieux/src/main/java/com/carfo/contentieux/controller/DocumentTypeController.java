package com.carfo.contentieux.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Document types", description = "Liste des types de documents supportés pour les formulaires")
@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeController {

    @Operation(summary = "Récupérer la liste des types de document")
    @GetMapping
    public List<String> getDocumentTypes() {
        // Keep in sync with frontend expectations / test dataset
        return Arrays.asList(
                "requete",
                "piece_justificative",
                "pv_audience",
                "releve_general_service",
                "indice",
                "acte_carriere",
                "assignation",
                "convocation",
                "decision_justice"
        );
    }
}
