package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.DossierJuriste;
import com.carfo.contentieux.model.DossierJuristeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierJuristeRepository extends JpaRepository<DossierJuriste, DossierJuristeId> {
    List<DossierJuriste> findByDossier_NumeroDossier(String numeroDossier);
}
