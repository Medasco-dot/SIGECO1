package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Cabinet;
import com.carfo.contentieux.repository.CabinetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CabinetService {
    private final CabinetRepository cabinetRepository;

    public CabinetService(CabinetRepository cabinetRepository) { this.cabinetRepository = cabinetRepository; }

    public List<Cabinet> getAllCabinets() { return cabinetRepository.findAll(); }
    public Optional<Cabinet> getCabinetById(String id) { return cabinetRepository.findById(id); }
    public Cabinet createCabinet(Cabinet cabinet) { return cabinetRepository.save(cabinet); }

    public Cabinet updateCabinet(String identifiantCabinet, Cabinet cabinet) {
        Cabinet existant = cabinetRepository.findById(identifiantCabinet)
                .orElseThrow(() -> new ResourceNotFoundException("Cabinet introuvable avec l'identifiant " + identifiantCabinet));
        existant.setNomCabinet(cabinet.getNomCabinet());
        existant.setMail(cabinet.getMail());
        existant.setTelephone(cabinet.getTelephone());
        existant.setAdresse(cabinet.getAdresse());
        return cabinetRepository.save(existant);
    }

    public void deleteCabinet(String id) { cabinetRepository.deleteById(id); }
}
