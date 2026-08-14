package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.TypeContentieuxDTO;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.service.TypeContentieuxService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Types de contentieux")
@RestController
@RequestMapping("/api/types-contentieux")
public class TypeContentieuxController {
    private final TypeContentieuxService typeContentieuxService;
    public TypeContentieuxController(TypeContentieuxService typeContentieuxService) { this.typeContentieuxService = typeContentieuxService; }

    @Operation(summary = "Récupérer tous les types de contentieux")
    @GetMapping public List<TypeContentieux> getAll() { return typeContentieuxService.getAllTypesContentieux(); }

    @Operation(summary = "Récupérer un type de contentieux par son ID")
    @GetMapping("/{id}") public ResponseEntity<TypeContentieux> getById(@PathVariable Integer id) { return ResponseEntity.of(typeContentieuxService.getTypeContentieuxById(id)); }

    @Operation(summary = "Créer un nouveau type de contentieux")
    @PostMapping public ResponseEntity<TypeContentieux> create(@Valid @RequestBody TypeContentieuxDTO dto) {
        TypeContentieux t = new TypeContentieux();
        t.setNature(dto.getNature());
        return ResponseEntity.status(HttpStatus.CREATED).body(typeContentieuxService.createTypeContentieux(t));
    }

    @Operation(summary = "Modifier un type de contentieux existant")
    @PutMapping("/{id}") public ResponseEntity<TypeContentieux> update(@PathVariable Integer id, @Valid @RequestBody TypeContentieuxDTO dto) {
        TypeContentieux t = new TypeContentieux();
        t.setNature(dto.getNature());
        return ResponseEntity.ok(typeContentieuxService.updateTypeContentieux(id, t));
    }

    @Operation(summary = "Supprimer un type de contentieux")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { typeContentieuxService.deleteTypeContentieux(id); return ResponseEntity.noContent().build(); }
}
