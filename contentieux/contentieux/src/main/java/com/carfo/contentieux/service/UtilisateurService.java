package com.carfo.contentieux.service;

import com.carfo.contentieux.dto.LoginResponse;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Juriste;
import com.carfo.contentieux.model.Role;
import com.carfo.contentieux.model.Utilisateur;
import com.carfo.contentieux.repository.JuristeRepository;
import com.carfo.contentieux.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final JuristeRepository juristeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, JuristeRepository juristeRepository,
                               PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.juristeRepository = juristeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> getUtilisateurById(Integer id) {
        return utilisateurRepository.findById(id);
    }

    public Utilisateur createUtilisateur(Utilisateur utilisateur, String matriculeJuriste) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        utilisateur.setJuriste(resolveJuriste(matriculeJuriste));
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur updateUtilisateur(Integer id, Utilisateur utilisateur, String matriculeJuriste) {
        Utilisateur existant = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id " + id));
        existant.setIdentifiant(utilisateur.getIdentifiant());
        existant.setNom(utilisateur.getNom());
        existant.setPrenom(utilisateur.getPrenom());
        existant.setRole(utilisateur.getRole());
        existant.setJuriste(resolveJuriste(matriculeJuriste));
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isBlank()) {
            existant.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }
        return utilisateurRepository.save(existant);
    }

    public void deleteUtilisateur(Integer id) {
        utilisateurRepository.deleteById(id);
    }

    public LoginResponse authenticate(String identifiant, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiant ou mot de passe invalide"));

        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiant ou mot de passe invalide");
        }

        String token = jwtService.generateToken(utilisateur.getIdentifiant(), utilisateur.getRole().name());
        String matriculeJuriste = utilisateur.getJuriste() != null ? utilisateur.getJuriste().getMatricule() : null;
        return new LoginResponse(token, utilisateur.getIdentifiant(), utilisateur.getRole().name(), matriculeJuriste);
    }

    private Juriste resolveJuriste(String matriculeJuriste) {
        if (matriculeJuriste == null || matriculeJuriste.isBlank()) {
            return null;
        }
        return juristeRepository.findById(matriculeJuriste)
                .orElseThrow(() -> new ResourceNotFoundException("Juriste introuvable avec le matricule " + matriculeJuriste));
    }
}
