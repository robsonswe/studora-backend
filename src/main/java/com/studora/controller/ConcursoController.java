package com.studora.controller;

import com.studora.dto.ConcursoDto;
import com.studora.service.ConcursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/concursos")
@CrossOrigin(origins = "*")
public class ConcursoController {
    
    @Autowired
    private ConcursoService concursoService;
    
    @GetMapping
    public ResponseEntity<List<ConcursoDto>> getAllConcursos() {
        List<ConcursoDto> concursos = concursoService.getAllConcursos();
        return ResponseEntity.ok(concursos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConcursoDto> getConcursoById(@PathVariable Long id) {
        ConcursoDto concurso = concursoService.getConcursoById(id);
        return ResponseEntity.ok(concurso);
    }
    
    @PostMapping
    public ResponseEntity<ConcursoDto> createConcurso(@Valid @RequestBody ConcursoDto concursoDto) {
        ConcursoDto createdConcurso = concursoService.createConcurso(concursoDto);
        return new ResponseEntity<>(createdConcurso, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ConcursoDto> updateConcurso(@PathVariable Long id, 
                                                     @Valid @RequestBody ConcursoDto concursoDto) {
        ConcursoDto updatedConcurso = concursoService.updateConcurso(id, concursoDto);
        return ResponseEntity.ok(updatedConcurso);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcurso(@PathVariable Long id) {
        concursoService.deleteConcurso(id);
        return ResponseEntity.noContent().build();
    }
}