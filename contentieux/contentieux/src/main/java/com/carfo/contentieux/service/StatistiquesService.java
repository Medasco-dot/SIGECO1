package com.carfo.contentieux.service;

import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.EtapeDossierRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {
    private final DossierRepository dossierRepository;
    private final EtapeDossierRepository etapeDossierRepository;

    public StatistiquesService(DossierRepository dossierRepository, EtapeDossierRepository etapeDossierRepository) {
        this.dossierRepository = dossierRepository;
        this.etapeDossierRepository = etapeDossierRepository;
    }

    public long getNombreTotalDossiers() { return dossierRepository.count(); }

    public Map<String, Long> getDossiersParType() {
        return dossierRepository.findAll().stream().collect(Collectors.groupingBy(
                dossier -> dossier.getTypeContentieux().getNature().name(), Collectors.counting()));
    }

    public Map<String, Long> getEtapesParStatut() {
        return etapeDossierRepository.findAll().stream().collect(Collectors.groupingBy(
                etape -> etape.getEtape().name(), Collectors.counting()));
    }
}
