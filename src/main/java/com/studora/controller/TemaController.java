package com.studora.controller;

import com.studora.dto.TemaDto;
import com.studora.service.TemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/temas")
@CrossOrigin(origins = "*")
public class TemaController {
    
    @Autowired
    private TemaService temaService;
    
    @GetMapping
    public ResponseEntity<List<TemaDto>> getAllTemas() {
        List<TemaDto> temas = temaService.getAllTemas();
        return ResponseEntity.ok(temas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemaDto> getTemaById(@PathVariable Long id) {
        TemaDto tema = temaService.getTemaById(id);
        return ResponseEntity.ok(tema);
    }
    
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<TemaDto>> getTemasByDisciplinaId(@PathVariable Long disciplinaId) {
        List<TemaDto> temas = temaService.getTemasByDisciplinaId(disciplinaId);
        return ResponseEntity.ok(temas);
    }
    
    @PostMapping
    public ResponseEntity<TemaDto> createTema(@Valid @RequestBody TemaDto temaDto) {
        TemaDto createdTema = temaService.createTema(temaDto);
        return new ResponseEntity<>(createdTema, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TemaDto> updateTema(@PathVariable Long id, 
                                            @Valid @RequestBody TemaDto temaDto) {
        TemaDto updatedTema = temaService.updateTema(id, temaDto);
        return ResponseEntity.ok(updatedTema);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTema(@PathVariable Long id) {
        temaService.deleteTema(id);
        return ResponseEntity.noContent().build();
    }
}