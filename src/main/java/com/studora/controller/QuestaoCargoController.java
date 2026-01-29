package com.studora.controller;

import com.studora.dto.QuestaoCargoDto;
import com.studora.service.QuestaoCargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questao-cargos")
public class QuestaoCargoController {

    @Autowired
    private QuestaoCargoService questaoCargoService;

    @GetMapping
    public List<QuestaoCargoDto> getAllQuestaoCargos() {
        return questaoCargoService.findAll();
    }

    @GetMapping("/{id}")
    public QuestaoCargoDto getQuestaoCargoById(@PathVariable Long id) {
        return questaoCargoService.findById(id);
    }

    @PostMapping
    public QuestaoCargoDto createQuestaoCargo(@RequestBody QuestaoCargoDto questaoCargoDto) {
        return questaoCargoService.save(questaoCargoDto);
    }

    @PutMapping("/{id}")
    public QuestaoCargoDto updateQuestaoCargo(@PathVariable Long id, @RequestBody QuestaoCargoDto questaoCargoDto) {
        questaoCargoDto.setId(id);
        return questaoCargoService.save(questaoCargoDto);
    }

    @DeleteMapping("/{id}")
    public void deleteQuestaoCargo(@PathVariable Long id) {
        questaoCargoService.deleteById(id);
    }
}
