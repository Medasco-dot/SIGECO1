package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.DossierCabinet;
import com.carfo.contentieux.model.DossierCabinetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierCabinetRepository extends JpaRepository<DossierCabinet, DossierCabinetId> {
    List<DossierCabinet> findByDossier_NumeroDossier(String numeroDossier);
}
