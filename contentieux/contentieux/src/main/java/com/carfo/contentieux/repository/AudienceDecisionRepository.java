package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.AudienceDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AudienceDecisionRepository extends JpaRepository<AudienceDecision, Integer>{
    List<AudienceDecision> findByDossier_NumeroDossier(String numeroDossier);
    List<AudienceDecision> findByTypeEtape(AudienceDecision.TypeEtape typeEtape);
    List<AudienceDecision> findByDateBetween(LocalDate debut, LocalDate fin);
    List<AudienceDecision> findByIssuePourCarfo(AudienceDecision.IssuePourCarfo issue);
}
