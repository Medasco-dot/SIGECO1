package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.AudienceDecision;
import com.carfo.contentieux.repository.AudienceDecisionRepository;
import com.carfo.contentieux.util.DateGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AudienceDecisionService {
    private final AudienceDecisionRepository audienceDecisionRepository;
    private final DossierService dossierService;

    public AudienceDecisionService(AudienceDecisionRepository audienceDecisionRepository,
                                   DossierService dossierService) {
        this.audienceDecisionRepository = audienceDecisionRepository;
        this.dossierService = dossierService;
    }

    public List<AudienceDecision> getAllAudienceDecisions() { return audienceDecisionRepository.findAll(); }
    public List<AudienceDecision> getAudienceDecisionsByDossier(String numeroDossier) { return audienceDecisionRepository.findByEtapeDossier_Dossier_NumeroDossier(numeroDossier); }
    public Optional<AudienceDecision> getAudienceDecisionById(Integer id) { return audienceDecisionRepository.findById(id); }

    @Transactional
    public AudienceDecision createAudienceDecision(AudienceDecision audienceDecision) {
        DateGuard.checkReasonable(audienceDecision.getDate(), "date");
        AudienceDecision saved = audienceDecisionRepository.save(audienceDecision);
        if (saved.getEtapeDossier() != null && saved.getEtapeDossier().getNumeroDossier() != null) {
            dossierService.recalculerFraisJustice(saved.getEtapeDossier().getNumeroDossier());
        }
        return saved;
    }

    public List<AudienceDecision> findByTypeEtape(AudienceDecision.TypeEtape type) { return audienceDecisionRepository.findByTypeEtape(type); }
    public List<AudienceDecision> findByDateBetween(java.time.LocalDate debut, java.time.LocalDate fin) { return audienceDecisionRepository.findByDateBetween(debut, fin); }
    public List<AudienceDecision> findByIssue(AudienceDecision.IssuePourCarfo issue) { return audienceDecisionRepository.findByIssuePourCarfo(issue); }

    @Transactional
    public AudienceDecision updateAudienceDecision(Integer id, AudienceDecision audienceDecision) {
        AudienceDecision existant = audienceDecisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audience/Décision introuvable avec l'id " + id));
        DateGuard.checkReasonable(audienceDecision.getDate(), "date");
        existant.setDate(audienceDecision.getDate());
        existant.setLieuAudience(audienceDecision.getLieuAudience());
        existant.setTypeEtape(audienceDecision.getTypeEtape());
        existant.setNatureDecision(audienceDecision.getNatureDecision());
        existant.setResumeDecision(audienceDecision.getResumeDecision());
        existant.setIssuePourCarfo(audienceDecision.getIssuePourCarfo());
        existant.setMontantObtenu(audienceDecision.getMontantObtenu());
        existant.setMontantDu(audienceDecision.getMontantDu());
        existant.setFraisJustice(audienceDecision.getFraisJustice());
        if (audienceDecision.getEtapeDossier() != null) {
            existant.setEtapeDossier(audienceDecision.getEtapeDossier());
        }
        AudienceDecision updated = audienceDecisionRepository.save(existant);
        if (updated.getEtapeDossier() != null && updated.getEtapeDossier().getNumeroDossier() != null) {
            dossierService.recalculerFraisJustice(updated.getEtapeDossier().getNumeroDossier());
        }
        return updated;
    }

    @Transactional
    public void deleteAudienceDecision(Integer id) {
        Optional<AudienceDecision> opt = audienceDecisionRepository.findById(id);
        String numeroDossier = null;
        if (opt.isPresent() && opt.get().getEtapeDossier() != null) {
            numeroDossier = opt.get().getEtapeDossier().getNumeroDossier();
        }
        audienceDecisionRepository.deleteById(id);
        if (numeroDossier != null) {
            dossierService.recalculerFraisJustice(numeroDossier);
        }
    }
}
