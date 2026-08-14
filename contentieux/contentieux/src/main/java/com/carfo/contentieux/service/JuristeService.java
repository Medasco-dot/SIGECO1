package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Juriste;
import com.carfo.contentieux.repository.JuristeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JuristeService {
    private final JuristeRepository juristeRepository;

    public JuristeService(JuristeRepository juristeRepository) { this.juristeRepository = juristeRepository; }

    public List<Juriste> getAllJuristes() { return juristeRepository.findAll(); }
    public Optional<Juriste> getJuristeById(String matricule) { return juristeRepository.findById(matricule); }
    public Juriste createJuriste(Juriste juriste) { return juristeRepository.save(juriste); }

    public Juriste updateJuriste(String matricule, Juriste juriste) {
        Juriste existant = juristeRepository.findById(matricule)
                .orElseThrow(() -> new ResourceNotFoundException("Juriste introuvable avec le matricule " + matricule));
        existant.setNom(juriste.getNom());
        existant.setPrenoms(juriste.getPrenoms());
        existant.setSpecialite(juriste.getSpecialite());
        return juristeRepository.save(existant);
    }

    public void deleteJuriste(String matricule) { juristeRepository.deleteById(matricule); }
}
