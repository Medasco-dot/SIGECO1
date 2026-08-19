package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

/**
 * Volontairement basé sur {@link Repository} et non {@link org.springframework.data.jpa.repository.JpaRepository} :
 * seules save/findAll/findBy sont exposées, aucune méthode de suppression ou de mise à jour
 * en masse. Le journal d'audit ne doit jamais pouvoir être modifié après coup.
 */
public interface AuditLogRepository extends Repository<AuditLog, Long> {

    AuditLog save(AuditLog auditLog);

    Page<AuditLog> findByTypeEntiteAndIdentifiantEntiteOrderByHorodatageDesc(
            String typeEntite, String identifiantEntite, Pageable pageable);

    Page<AuditLog> findAllByOrderByHorodatageDesc(Pageable pageable);
}
