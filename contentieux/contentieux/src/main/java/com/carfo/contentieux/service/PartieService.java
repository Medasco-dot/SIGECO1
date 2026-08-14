package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Partie;
import com.carfo.contentieux.repository.PartieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartieService {
    private final PartieRepository partieRepository;

    public PartieService(PartieRepository partieRepository) { this.partieRepository = partieRepository; }

    public List<Partie> getAllParties() { return partieRepository.findAll(); }
    public Optional<Partie> getPartieById(Integer id) { return partieRepository.findById(id); }
    public Partie createPartie(Partie partie) { return partieRepository.save(partie); }

    public Partie updatePartie(Integer id, Partie partie) {
        Partie existant = partieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partie introuvable avec l'id " + id));
        existant.setNom(partie.getNom());
        existant.setPrenom(partie.getPrenom());
        existant.setNumeroCnib(partie.getNumeroCnib());
        existant.setStatutMatrimonial(partie.getStatutMatrimonial());
        return partieRepository.save(existant);
    }

    public void deletePartie(Integer id) { partieRepository.deleteById(id); }
}
