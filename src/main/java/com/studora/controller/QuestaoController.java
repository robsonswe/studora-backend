package com.studora.controller;

import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.service.QuestaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "*")
public class QuestaoController {

    @Autowired
    private QuestaoService questaoService;

    @GetMapping
    public ResponseEntity<List<QuestaoDto>> getAllQuestoes() {
        List<QuestaoDto> questoes = questaoService.getAllQuestoes();
        return ResponseEntity.ok(questoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestaoDto> getQuestaoById(@PathVariable Long id) {
        QuestaoDto questao = questaoService.getQuestaoById(id);
        return ResponseEntity.ok(questao);
    }

    @GetMapping("/concurso/{concursoId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesByConcursoId(@PathVariable Long concursoId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesByConcursoId(concursoId);
        return ResponseEntity.ok(questoes);
    }

    @GetMapping("/subtema/{subtemaId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesBySubtemaId(@PathVariable Long subtemaId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesBySubtemaId(subtemaId);
        return ResponseEntity.ok(questoes);
    }

    @GetMapping("/nao-anuladas")
    public ResponseEntity<List<QuestaoDto>> getQuestoesNaoAnuladas() {
        List<QuestaoDto> questoes = questaoService.getQuestoesNaoAnuladas();
        return ResponseEntity.ok(questoes);
    }

    @PostMapping
    public ResponseEntity<QuestaoDto> createQuestao(@Valid @RequestBody QuestaoDto questaoDto) {
        QuestaoDto createdQuestao = questaoService.createQuestao(questaoDto);
        return new ResponseEntity<>(createdQuestao, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestaoDto> updateQuestao(@PathVariable Long id,
                                                 @Valid @RequestBody QuestaoDto questaoDto) {
        QuestaoDto updatedQuestao = questaoService.updateQuestao(id, questaoDto);
        return ResponseEntity.ok(updatedQuestao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(@PathVariable Long id) {
        questaoService.deleteQuestao(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints for managing cargo associations
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<QuestaoCargoDto>> getCargosByQuestao(@PathVariable Long id) {
        List<QuestaoCargoDto> cargos = questaoService.getCargosByQuestaoId(id);
        return ResponseEntity.ok(cargos);
    }

    @PostMapping("/{id}/cargos")
    public ResponseEntity<QuestaoCargoDto> addCargoToQuestao(@PathVariable Long id, @RequestBody QuestaoCargoDto questaoCargoDto) {
        questaoCargoDto.setQuestaoId(id); // Ensure the questao ID matches the path variable
        QuestaoCargoDto createdQuestaoCargo = questaoService.addCargoToQuestao(questaoCargoDto);
        return new ResponseEntity<>(createdQuestaoCargo, HttpStatus.CREATED);
    }

    @DeleteMapping("/{questaoId}/cargos/{concursoCargoId}")
    public ResponseEntity<Void> removeCargoFromQuestao(@PathVariable Long questaoId, @PathVariable Long concursoCargoId) {
        questaoService.removeCargoFromQuestao(questaoId, concursoCargoId);
        return ResponseEntity.noContent().build();
    }
}