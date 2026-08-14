package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.Implication;
import com.carfo.contentieux.model.ImplicationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImplicationRepository extends JpaRepository<Implication, ImplicationId>{
    List<Implication> findByDossier_NumeroDossier(String numeroDossier);
}
