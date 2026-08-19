package com.carfo.contentieux.controller;

import com.carfo.contentieux.model.AuditLog;
import com.carfo.contentieux.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit", description = "Consultation de la piste d'audit (lecture seule, aucune ecriture/suppression possible)")
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Operation(summary = "Historique d'audit d'un dossier (creation, modifications, suppression)")
    @GetMapping("/dossier/{numeroDossier}")
    public Page<AuditLog> historiqueDossier(@PathVariable String numeroDossier,
                                             @PageableDefault(size = 50) Pageable pageable) {
        return auditLogRepository.findByTypeEntiteAndIdentifiantEntiteOrderByHorodatageDesc("Dossier", numeroDossier, pageable);
    }
}
