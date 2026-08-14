package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.EtapeDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapeDossierRepository extends JpaRepository<EtapeDossier, Integer> {
    List<EtapeDossier> findByDossier_NumeroDossier(String numeroDossier);
}
