package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.TypeContentieux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeContentieuxRepository extends JpaRepository<TypeContentieux, Integer>{
    Optional<TypeContentieux> findByNature(TypeContentieux.Nature nature);
}
