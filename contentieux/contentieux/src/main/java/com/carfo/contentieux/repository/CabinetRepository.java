package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.Cabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CabinetRepository extends JpaRepository<Cabinet, String>{
}
