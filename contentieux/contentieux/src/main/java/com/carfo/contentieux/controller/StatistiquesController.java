package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.StatistiquesSynthese;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.service.StatistiquesExportService;
import com.carfo.contentieux.service.StatistiquesService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Tag(name = "Statistiques", description = "Indicateurs et tableaux de bord")
@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;
    private final StatistiquesExportService statistiquesExportService;

    public StatistiquesController(StatistiquesService statistiquesService, StatistiquesExportService statistiquesExportService) {
        this.statistiquesService = statistiquesService;
        this.statistiquesExportService = statistiquesExportService;
    }

    @Operation(summary = "Tableau de bord synthèse (KPIs + répartitions + TOP 5 risques)")
    @GetMapping("/synthese")
    public StatistiquesSynthese synthese() {
        return statistiquesService.synthese();
    }

    @Operation(summary = "Nombre total de dossiers")
    @GetMapping("/nombre-dossiers")
    public Long getNombreTotalDossiers() {
        return statistiquesService.nombreTotalDossiers();
    }

    @Operation(summary = "Répartition des dossiers par type de contentieux")
    @GetMapping("/dossiers-par-type")
    public Map<TypeContentieux.Nature, Long> getDossiersParType() {
        return statistiquesService.dossiersParType();
    }

    @Operation(summary = "Répartition des dossiers par étape (dernière étape par dossier)")
    @GetMapping("/etapes-par-statut")
    public Map<EtapeDossier.Etape, Long> getEtapesParStatut() {
        return statistiquesService.etapesParStatut();
    }

    @Operation(summary = "Export synthèse en PDF")
    @GetMapping(value = "/export/pdf")
    public ResponseEntity<byte[]> exportPdf() throws IOException {
        StatistiquesSynthese s = statistiquesService.synthese();
        byte[] bytes = statistiquesExportService.generatePdf(s);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "statistiques-contentieux-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".pdf";
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @Operation(summary = "Export synthèse en Excel (XLSX)")
    @GetMapping(value = "/export/excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        StatistiquesSynthese s = statistiquesService.synthese();
        byte[] bytes = statistiquesExportService.generateExcel(s);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String filename = "statistiques-contentieux-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xlsx";
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @Operation(summary = "Export synthèse en Word (DOCX)")
    @GetMapping(value = "/export/word")
    public ResponseEntity<byte[]> exportWord() throws IOException {
        StatistiquesSynthese s = statistiquesService.synthese();
        byte[] bytes = statistiquesExportService.generateWord(s);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        String filename = "statistiques-contentieux-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".docx";
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
