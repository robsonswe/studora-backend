package com.studora.controller;

import com.studora.dto.ConcursoCargoDto;
import com.studora.service.ConcursoCargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concurso-cargos")
public class ConcursoCargoController {

    @Autowired
    private ConcursoCargoService concursoCargoService;

    @GetMapping
    public List<ConcursoCargoDto> getAllConcursoCargos() {
        return concursoCargoService.findAll();
    }

    @GetMapping("/{id}")
    public ConcursoCargoDto getConcursoCargoById(@PathVariable Long id) {
        return concursoCargoService.findById(id);
    }

    @PostMapping
    public ConcursoCargoDto createConcursoCargo(@RequestBody ConcursoCargoDto concursoCargoDto) {
        return concursoCargoService.save(concursoCargoDto);
    }

    @PutMapping("/{id}")
    public ConcursoCargoDto updateConcursoCargo(@PathVariable Long id, @RequestBody ConcursoCargoDto concursoCargoDto) {
        concursoCargoDto.setId(id);
        return concursoCargoService.save(concursoCargoDto);
    }

    @DeleteMapping("/{id}")
    public void deleteConcursoCargo(@PathVariable Long id) {
        concursoCargoService.deleteById(id);
    }
}
