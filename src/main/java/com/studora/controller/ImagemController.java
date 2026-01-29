package com.studora.controller;

import com.studora.dto.ImagemDto;
import com.studora.service.ImagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/imagens")
@CrossOrigin(origins = "*")
public class ImagemController {
    
    @Autowired
    private ImagemService imagemService;
    
    @GetMapping
    public ResponseEntity<List<ImagemDto>> getAllImagens() {
        List<ImagemDto> imagens = imagemService.getAllImagens();
        return ResponseEntity.ok(imagens);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ImagemDto> getImagemById(@PathVariable Long id) {
        ImagemDto imagem = imagemService.getImagemById(id);
        return ResponseEntity.ok(imagem);
    }
    
    @PostMapping
    public ResponseEntity<ImagemDto> createImagem(@Valid @RequestBody ImagemDto imagemDto) {
        ImagemDto createdImagem = imagemService.createImagem(imagemDto);
        return new ResponseEntity<>(createdImagem, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ImagemDto> updateImagem(@PathVariable Long id, 
                                                @Valid @RequestBody ImagemDto imagemDto) {
        ImagemDto updatedImagem = imagemService.updateImagem(id, imagemDto);
        return ResponseEntity.ok(updatedImagem);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImagem(@PathVariable Long id) {
        imagemService.deleteImagem(id);
        return ResponseEntity.noContent().build();
    }
}