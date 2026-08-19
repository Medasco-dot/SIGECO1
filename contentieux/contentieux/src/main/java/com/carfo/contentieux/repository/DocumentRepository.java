package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByDossier_NumeroDossier(String numeroDossier);
    List<Document> findByTypeDocument(String typeDocument);
    List<Document> findByDateAjoutBetween(LocalDate debut, LocalDate fin);
    List<Document> findByTypeDocumentAndDossier_NumeroDossier(String typeDocument, String numeroDossier);
    boolean existsByTypeDocument(String typeDocument);
}
