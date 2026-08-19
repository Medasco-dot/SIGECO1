package com.carfo.contentieux.service;

import com.carfo.contentieux.model.AuditLog;
import com.carfo.contentieux.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void enregistrer(AuditLog.Action action, String typeEntite, String identifiantEntite, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifiant = auth != null ? auth.getName() : "systeme";
        String role = auth != null
                ? auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("INCONNU")
                : "SYSTEME";
        auditLogRepository.save(new AuditLog(identifiant, role, action, typeEntite, identifiantEntite, details));
    }
}
