package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.repository.EtapeDossierRepository;
import com.carfo.contentieux.util.DateGuard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EtapeDossierService {
    private final EtapeDossierRepository etapeDossierRepository;

    public EtapeDossierService(EtapeDossierRepository etapeDossierRepository) { this.etapeDossierRepository = etapeDossierRepository; }

    public List<EtapeDossier> getAllEtapesDossier() { return etapeDossierRepository.findAll(); }
    public List<EtapeDossier> getEtapesByDossier(String numeroDossier) { return etapeDossierRepository.findByDossier_NumeroDossier(numeroDossier); }
    public Optional<EtapeDossier> getEtapeDossierById(Integer id) { return etapeDossierRepository.findById(id); }

    public EtapeDossier createEtapeDossier(EtapeDossier etapeDossier) {
        validateDates(etapeDossier.getDateDebut(), etapeDossier.getDateFin());
        return etapeDossierRepository.save(etapeDossier);
    }

    public EtapeDossier updateEtapeDossier(Integer id, EtapeDossier etapeDossier) {
        EtapeDossier existant = etapeDossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étape dossier introuvable avec l'id " + id));
        validateDates(etapeDossier.getDateDebut(), etapeDossier.getDateFin());
        existant.setEtape(etapeDossier.getEtape());
        existant.setDateDebut(etapeDossier.getDateDebut());
        existant.setDateFin(etapeDossier.getDateFin());
        if (etapeDossier.getDossier() != null) {
            existant.setDossier(etapeDossier.getDossier());
        }
        return etapeDossierRepository.save(existant);
    }

    private void validateDates(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        DateGuard.checkReasonable(dateDebut, "dateDebut");
        DateGuard.checkReasonable(dateFin, "dateFin");
        DateGuard.checkOrder(dateDebut, dateFin, "dateDebut", "dateFin");
    }

    public void deleteEtapeDossier(Integer id) { etapeDossierRepository.deleteById(id); }
}
