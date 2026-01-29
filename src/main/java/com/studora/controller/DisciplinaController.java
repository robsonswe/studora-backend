package com.studora.controller;

import com.studora.dto.DisciplinaDto;
import com.studora.service.DisciplinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
@CrossOrigin(origins = "*")
public class DisciplinaController {
    
    @Autowired
    private DisciplinaService disciplinaService;
    
    @GetMapping
    public ResponseEntity<List<DisciplinaDto>> getAllDisciplinas() {
        List<DisciplinaDto> disciplinas = disciplinaService.getAllDisciplinas();
        return ResponseEntity.ok(disciplinas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaDto> getDisciplinaById(@PathVariable Long id) {
        DisciplinaDto disciplina = disciplinaService.getDisciplinaById(id);
        return ResponseEntity.ok(disciplina);
    }
    
    @PostMapping
    public ResponseEntity<DisciplinaDto> createDisciplina(@Valid @RequestBody DisciplinaDto disciplinaDto) {
        DisciplinaDto createdDisciplina = disciplinaService.createDisciplina(disciplinaDto);
        return new ResponseEntity<>(createdDisciplina, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaDto> updateDisciplina(@PathVariable Long id, 
                                                        @Valid @RequestBody DisciplinaDto disciplinaDto) {
        DisciplinaDto updatedDisciplina = disciplinaService.updateDisciplina(id, disciplinaDto);
        return ResponseEntity.ok(updatedDisciplina);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisciplina(@PathVariable Long id) {
        disciplinaService.deleteDisciplina(id);
        return ResponseEntity.noContent().build();
    }
}