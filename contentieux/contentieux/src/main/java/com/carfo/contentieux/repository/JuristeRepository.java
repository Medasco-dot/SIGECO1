package com.carfo.contentieux.repository;


import com.carfo.contentieux.model.Juriste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JuristeRepository extends JpaRepository<Juriste, String>{
}
