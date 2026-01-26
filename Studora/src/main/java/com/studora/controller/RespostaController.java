package com.studora.controller;

import com.studora.dto.RespostaDto;
import com.studora.service.RespostaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/respostas")
@CrossOrigin(origins = "*")
public class RespostaController {
    
    @Autowired
    private RespostaService respostaService;
    
    @GetMapping
    public ResponseEntity<List<RespostaDto>> getAllRespostas() {
        List<RespostaDto> respostas = respostaService.getAllRespostas();
        return ResponseEntity.ok(respostas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RespostaDto> getRespostaById(@PathVariable Long id) {
        RespostaDto resposta = respostaService.getRespostaById(id);
        return ResponseEntity.ok(resposta);
    }
    
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<List<RespostaDto>> getRespostasByQuestaoId(@PathVariable Long questaoId) {
        List<RespostaDto> respostas = respostaService.getRespostasByQuestaoId(questaoId);
        return ResponseEntity.ok(respostas);
    }
    
    @GetMapping("/alternativa/{alternativaId}")
    public ResponseEntity<List<RespostaDto>> getRespostasByAlternativaId(@PathVariable Long alternativaId) {
        List<RespostaDto> respostas = respostaService.getRespostasByAlternativaId(alternativaId);
        return ResponseEntity.ok(respostas);
    }
    
    @PostMapping
    public ResponseEntity<RespostaDto> createResposta(@Valid @RequestBody RespostaDto respostaDto) {
        RespostaDto createdResposta = respostaService.createResposta(respostaDto);
        return new ResponseEntity<>(createdResposta, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RespostaDto> updateResposta(@PathVariable Long id, 
                                                   @Valid @RequestBody RespostaDto respostaDto) {
        RespostaDto updatedResposta = respostaService.updateResposta(id, respostaDto);
        return ResponseEntity.ok(updatedResposta);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResposta(@PathVariable Long id) {
        respostaService.deleteResposta(id);
        return ResponseEntity.noContent().build();
    }
}