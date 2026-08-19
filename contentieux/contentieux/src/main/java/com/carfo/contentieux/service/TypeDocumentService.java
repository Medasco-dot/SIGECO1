package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.DuplicateResourceException;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.TypeDocument;
import com.carfo.contentieux.repository.DocumentRepository;
import com.carfo.contentieux.repository.TypeDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TypeDocumentService {

    private final TypeDocumentRepository typeDocumentRepository;
    private final DocumentRepository documentRepository;

    public TypeDocumentService(TypeDocumentRepository typeDocumentRepository, DocumentRepository documentRepository) {
        this.typeDocumentRepository = typeDocumentRepository;
        this.documentRepository = documentRepository;
    }

    public List<TypeDocument> getAll() {
        return typeDocumentRepository.findAll().stream()
                .sorted(Comparator.comparing(TypeDocument::getLibelle))
                .toList();
    }

    public Optional<TypeDocument> getByCode(String code) {
        return typeDocumentRepository.findById(code);
    }

    public TypeDocument create(TypeDocument typeDocument) {
        if (typeDocumentRepository.existsById(typeDocument.getCode())) {
            throw new DuplicateResourceException("Un type de document avec le code '" + typeDocument.getCode() + "' existe déjà");
        }
        return typeDocumentRepository.save(typeDocument);
    }

    public TypeDocument update(String code, TypeDocument typeDocument) {
        TypeDocument existant = typeDocumentRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Type de document introuvable avec le code " + code));
        existant.setLibelle(typeDocument.getLibelle());
        return typeDocumentRepository.save(existant);
    }

    public void delete(String code) {
        if (!typeDocumentRepository.existsById(code)) {
            throw new ResourceNotFoundException("Type de document introuvable avec le code " + code);
        }
        if (documentRepository.existsByTypeDocument(code)) {
            throw new DuplicateResourceException("Ce type de document est utilisé par au moins un document existant et ne peut pas être supprimé");
        }
        typeDocumentRepository.deleteById(code);
    }
}
