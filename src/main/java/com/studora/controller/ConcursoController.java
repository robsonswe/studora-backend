package com.studora.controller;

import com.studora.dto.ConcursoCargoDto;
import com.studora.dto.ConcursoDto;
import com.studora.service.ConcursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concursos")
public class ConcursoController {

    @Autowired
    private ConcursoService concursoService;

    @GetMapping
    public ResponseEntity<List<ConcursoDto>> getAllConcursos() {
        List<ConcursoDto> concursos = concursoService.findAll();
        return ResponseEntity.ok(concursos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConcursoDto> getConcursoById(@PathVariable Long id) {
        ConcursoDto concurso = concursoService.findById(id);
        return ResponseEntity.ok(concurso);
    }

    @PostMapping
    public ResponseEntity<ConcursoDto> createConcurso(@RequestBody ConcursoDto concursoDto) {
        ConcursoDto createdConcurso = concursoService.save(concursoDto);
        return new ResponseEntity<>(createdConcurso, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConcursoDto> updateConcurso(@PathVariable Long id,
                                                     @RequestBody ConcursoDto concursoDto) {
        concursoDto.setId(id);
        ConcursoDto updatedConcurso = concursoService.save(concursoDto);
        return ResponseEntity.ok(updatedConcurso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcurso(@PathVariable Long id) {
        concursoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints for managing cargo associations
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<ConcursoCargoDto>> getCargosByConcurso(@PathVariable Long id) {
        List<ConcursoCargoDto> cargos = concursoService.getCargosByConcursoId(id);
        return ResponseEntity.ok(cargos);
    }

    @PostMapping("/{id}/cargos")
    public ResponseEntity<ConcursoCargoDto> addCargoToConcurso(@PathVariable Long id, @RequestBody ConcursoCargoDto concursoCargoDto) {
        concursoCargoDto.setConcursoId(id); // Ensure the concurso ID matches the path variable
        ConcursoCargoDto createdConcursoCargo = concursoService.addCargoToConcurso(concursoCargoDto);
        return new ResponseEntity<>(createdConcursoCargo, HttpStatus.CREATED);
    }

    @DeleteMapping("/{concursoId}/cargos/{cargoId}")
    public ResponseEntity<Void> removeCargoFromConcurso(@PathVariable Long concursoId, @PathVariable Long cargoId) {
        concursoService.removeCargoFromConcurso(concursoId, cargoId);
        return ResponseEntity.noContent().build();
    }
}
