package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.DossierCabinet;
import com.carfo.contentieux.model.DossierCabinetId;
import com.carfo.contentieux.repository.DossierCabinetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DossierCabinetService {
    private final DossierCabinetRepository dossierCabinetRepository;

    public DossierCabinetService(DossierCabinetRepository dossierCabinetRepository) { this.dossierCabinetRepository = dossierCabinetRepository; }

    public List<DossierCabinet> getAllDossierCabinets() { return dossierCabinetRepository.findAll(); }
    public List<DossierCabinet> getDossierCabinetsByDossier(String numeroDossier) { return dossierCabinetRepository.findByDossier_NumeroDossier(numeroDossier); }
    public Optional<DossierCabinet> getDossierCabinetById(DossierCabinetId id) { return dossierCabinetRepository.findById(id); }
    public DossierCabinet createDossierCabinet(DossierCabinet dossierCabinet) { return dossierCabinetRepository.save(dossierCabinet); }

    public DossierCabinet updateDossierCabinet(DossierCabinetId id, DossierCabinet dossierCabinet) {
        DossierCabinet existant = dossierCabinetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Association Dossier-Cabinet introuvable pour le dossier " + id.getNumeroDossier() + " et le cabinet " + id.getIdentifiantCabinet()));
        existant.setNomAvocatReferent(dossierCabinet.getNomAvocatReferent());
        if (dossierCabinet.getCabinet() != null) {
            existant.setCabinet(dossierCabinet.getCabinet());
        }
        if (dossierCabinet.getDossier() != null) {
            existant.setDossier(dossierCabinet.getDossier());
        }
        return dossierCabinetRepository.save(existant);
    }

    public void deleteDossierCabinet(DossierCabinetId id) { dossierCabinetRepository.deleteById(id); }
}
