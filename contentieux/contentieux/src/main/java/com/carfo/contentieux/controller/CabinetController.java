package com.carfo.contentieux.controller;

import com.carfo.contentieux.dto.CabinetDTO;
import com.carfo.contentieux.model.Cabinet;
import com.carfo.contentieux.service.CabinetService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cabinets d'avocats")
@RestController
@RequestMapping("/api/cabinets")
public class CabinetController {
    private final CabinetService cabinetService;
    public CabinetController(CabinetService cabinetService) { this.cabinetService = cabinetService; }

    @Operation(summary = "Récupérer tous les cabinets d'avocats")
    @GetMapping public List<Cabinet> getAll() { return cabinetService.getAllCabinets(); }

    @Operation(summary = "Récupérer un cabinet par son identifiant")
    @GetMapping("/{id}") public ResponseEntity<Cabinet> getById(@PathVariable String id) { return ResponseEntity.of(cabinetService.getCabinetById(id)); }

    @Operation(summary = "Créer un nouveau cabinet d'avocats")
    @PostMapping public ResponseEntity<Cabinet> create(@Valid @RequestBody CabinetDTO dto) {
        Cabinet c = new Cabinet();
        c.setIdentifiantCabinet(dto.getIdentifiantCabinet());
        c.setNomCabinet(dto.getNomCabinet());
        c.setMail(dto.getMail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        return ResponseEntity.status(HttpStatus.CREATED).body(cabinetService.createCabinet(c));
    }

    @Operation(summary = "Modifier un cabinet d'avocats existant")
    @PutMapping("/{id}") public ResponseEntity<Cabinet> update(@PathVariable String id, @RequestBody CabinetDTO dto) {
        Cabinet c = new Cabinet();
        c.setNomCabinet(dto.getNomCabinet());
        c.setMail(dto.getMail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        return ResponseEntity.ok(cabinetService.updateCabinet(id, c));
    }

    @Operation(summary = "Supprimer un cabinet d'avocats")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable String id) { cabinetService.deleteCabinet(id); return ResponseEntity.noContent().build(); }
}
