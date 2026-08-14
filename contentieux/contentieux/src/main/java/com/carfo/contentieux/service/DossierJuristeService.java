package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.DossierJuriste;
import com.carfo.contentieux.model.DossierJuristeId;
import com.carfo.contentieux.repository.DossierJuristeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DossierJuristeService {
    private final DossierJuristeRepository dossierJuristeRepository;

    public DossierJuristeService(DossierJuristeRepository dossierJuristeRepository) { this.dossierJuristeRepository = dossierJuristeRepository; }

    public List<DossierJuriste> getAllDossierJuristes() { return dossierJuristeRepository.findAll(); }
    public List<DossierJuriste> getDossierJuristesByDossier(String numeroDossier) { return dossierJuristeRepository.findByDossier_NumeroDossier(numeroDossier); }
    public Optional<DossierJuriste> getDossierJuristeById(DossierJuristeId id) { return dossierJuristeRepository.findById(id); }
    public DossierJuriste createDossierJuriste(DossierJuriste dossierJuriste) { return dossierJuristeRepository.save(dossierJuriste); }

    public DossierJuriste updateDossierJuriste(DossierJuristeId id, DossierJuriste dossierJuriste) {
        DossierJuriste existant = dossierJuristeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Association Dossier-Juriste introuvable pour le dossier " + id.getNumeroDossier() + " et le juriste " + id.getMatricule()));
        if (dossierJuriste.getJuriste() != null) {
            existant.setJuriste(dossierJuriste.getJuriste());
        }
        if (dossierJuriste.getDossier() != null) {
            existant.setDossier(dossierJuriste.getDossier());
        }
        return dossierJuristeRepository.save(existant);
    }

    public void deleteDossierJuriste(DossierJuristeId id) { dossierJuristeRepository.deleteById(id); }
}
