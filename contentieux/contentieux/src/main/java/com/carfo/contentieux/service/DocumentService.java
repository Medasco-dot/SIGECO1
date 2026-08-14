package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.Document;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DossierRepository dossierRepository;

    public DocumentService(DocumentRepository documentRepository,
                           DossierRepository dossierRepository) {
        this.documentRepository = documentRepository;
        this.dossierRepository = dossierRepository;
    }

    public List<Document> getAllDocuments() { return documentRepository.findAll(); }
    public List<Document> getDocumentsByDossier(String numeroDossier) { return documentRepository.findByDossier_NumeroDossier(numeroDossier); }
    public Optional<Document> getDocumentById(Integer id) { return documentRepository.findById(id); }
    public Document createDocument(Document document) { return documentRepository.save(document); }
    public List<Document> findByType(String type) { return documentRepository.findByTypeDocument(type); }
    public List<Document> findByDateAjoutBetween(LocalDate debut, LocalDate fin) { return documentRepository.findByDateAjoutBetween(debut, fin); }
    public List<Document> findByTypeAndDossier(String type, String numeroDossier) { return documentRepository.findByTypeDocumentAndDossier_NumeroDossier(type, numeroDossier); }

    public Document updateMetadata(Integer id, String typeDocument, LocalDate dateAjout, String fichier, String numeroDossier) {
        Document existant = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable avec l'id " + id));
        if (typeDocument != null) {
            existant.setTypeDocument(typeDocument.trim());
        }
        if (dateAjout != null) {
            existant.setDateAjout(dateAjout);
        }
        if (fichier != null) {
            existant.setFichier(fichier);
        }
        if (numeroDossier != null) {
            Dossier dossier = dossierRepository.findById(numeroDossier)
                    .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + numeroDossier));
            existant.setDossier(dossier);
        }
        return documentRepository.save(existant);
    }

    @Transactional
    public void deleteDocument(Integer id) {
        documentRepository.deleteById(id);
    }
}
