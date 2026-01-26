package com.studora.controller;

import com.studora.dto.SubtemaDto;
import com.studora.service.SubtemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/subtemas")
@CrossOrigin(origins = "*")
public class SubtemaController {
    
    @Autowired
    private SubtemaService subtemaService;
    
    @GetMapping
    public ResponseEntity<List<SubtemaDto>> getAllSubtemas() {
        List<SubtemaDto> subtemas = subtemaService.getAllSubtemas();
        return ResponseEntity.ok(subtemas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SubtemaDto> getSubtemaById(@PathVariable Long id) {
        SubtemaDto subtema = subtemaService.getSubtemaById(id);
        return ResponseEntity.ok(subtema);
    }
    
    @GetMapping("/tema/{temaId}")
    public ResponseEntity<List<SubtemaDto>> getSubtemasByTemaId(@PathVariable Long temaId) {
        List<SubtemaDto> subtemas = subtemaService.getSubtemasByTemaId(temaId);
        return ResponseEntity.ok(subtemas);
    }
    
    @PostMapping
    public ResponseEntity<SubtemaDto> createSubtema(@Valid @RequestBody SubtemaDto subtemaDto) {
        SubtemaDto createdSubtema = subtemaService.createSubtema(subtemaDto);
        return new ResponseEntity<>(createdSubtema, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SubtemaDto> updateSubtema(@PathVariable Long id, 
                                                  @Valid @RequestBody SubtemaDto subtemaDto) {
        SubtemaDto updatedSubtema = subtemaService.updateSubtema(id, subtemaDto);
        return ResponseEntity.ok(updatedSubtema);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtema(@PathVariable Long id) {
        subtemaService.deleteSubtema(id);
        return ResponseEntity.noContent().build();
    }
}