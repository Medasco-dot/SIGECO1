package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Implication;
import com.carfo.contentieux.model.ImplicationId;
import com.carfo.contentieux.repository.ImplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImplicationService {
    private final ImplicationRepository implicationRepository;

    public ImplicationService(ImplicationRepository implicationRepository) { this.implicationRepository = implicationRepository; }

    public List<Implication> getAllImplications() { return implicationRepository.findAll(); }
    public List<Implication> getImplicationsByDossier(String numeroDossier) { return implicationRepository.findByDossier_NumeroDossier(numeroDossier); }
    public Optional<Implication> getImplicationById(ImplicationId id) { return implicationRepository.findById(id); }
    public Implication createImplication(Implication implication) { return implicationRepository.save(implication); }

    public Implication updateImplication(ImplicationId id, Implication implication) {
        Implication existant = implicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Implication introuvable pour le dossier " + id.getNumeroDossier() + " et la partie " + id.getIdPartie()));
        existant.setRole(implication.getRole());
        existant.setLienParente(implication.getLienParente());
        return implicationRepository.save(existant);
    }

    public void deleteImplication(ImplicationId id) { implicationRepository.deleteById(id); }
}
