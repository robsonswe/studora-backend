package com.studora.controller;

import com.studora.dto.AlternativaDto;
import com.studora.service.AlternativaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/alternativas")
@CrossOrigin(origins = "*")
public class AlternativaController {
    
    @Autowired
    private AlternativaService alternativaService;
    
    @GetMapping
    public ResponseEntity<List<AlternativaDto>> getAllAlternativas() {
        List<AlternativaDto> alternativas = alternativaService.getAllAlternativas();
        return ResponseEntity.ok(alternativas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AlternativaDto> getAlternativaById(@PathVariable Long id) {
        AlternativaDto alternativa = alternativaService.getAlternativaById(id);
        return ResponseEntity.ok(alternativa);
    }
    
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<List<AlternativaDto>> getAlternativasByQuestaoId(@PathVariable Long questaoId) {
        List<AlternativaDto> alternativas = alternativaService.getAlternativasByQuestaoId(questaoId);
        return ResponseEntity.ok(alternativas);
    }
    
    @GetMapping("/questao/{questaoId}/corretas")
    public ResponseEntity<List<AlternativaDto>> getAlternativasCorretasByQuestaoId(@PathVariable Long questaoId) {
        List<AlternativaDto> alternativas = alternativaService.getAlternativasCorretasByQuestaoId(questaoId);
        return ResponseEntity.ok(alternativas);
    }
    
    @PostMapping
    public ResponseEntity<AlternativaDto> createAlternativa(@Valid @RequestBody AlternativaDto alternativaDto) {
        AlternativaDto createdAlternativa = alternativaService.createAlternativa(alternativaDto);
        return new ResponseEntity<>(createdAlternativa, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AlternativaDto> updateAlternativa(@PathVariable Long id, 
                                                         @Valid @RequestBody AlternativaDto alternativaDto) {
        AlternativaDto updatedAlternativa = alternativaService.updateAlternativa(id, alternativaDto);
        return ResponseEntity.ok(updatedAlternativa);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlternativa(@PathVariable Long id) {
        alternativaService.deleteAlternativa(id);
        return ResponseEntity.noContent().build();
    }
}