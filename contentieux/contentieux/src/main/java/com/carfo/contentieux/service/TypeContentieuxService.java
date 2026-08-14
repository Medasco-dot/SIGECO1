package com.carfo.contentieux.service;

import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.repository.TypeContentieuxRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeContentieuxService {
    private final TypeContentieuxRepository typeContentieuxRepository;

    public TypeContentieuxService(TypeContentieuxRepository typeContentieuxRepository) { this.typeContentieuxRepository = typeContentieuxRepository; }

    public List<TypeContentieux> getAllTypesContentieux() { return typeContentieuxRepository.findAll(); }
    public Optional<TypeContentieux> getTypeContentieuxById(Integer id) { return typeContentieuxRepository.findById(id); }
    public TypeContentieux createTypeContentieux(TypeContentieux typeContentieux) { return typeContentieuxRepository.save(typeContentieux); }

    public TypeContentieux updateTypeContentieux(Integer id, TypeContentieux typeContentieux) {
        TypeContentieux existant = typeContentieuxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de contentieux introuvable avec l'id " + id));
        existant.setNature(typeContentieux.getNature());
        return typeContentieuxRepository.save(existant);
    }

    public void deleteTypeContentieux(Integer id) { typeContentieuxRepository.deleteById(id); }
}
