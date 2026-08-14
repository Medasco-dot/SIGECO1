package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.Partie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartieRepository extends JpaRepository<Partie, Integer>{
}
