package com.studora.controller;

import com.studora.dto.BancaDto;
import com.studora.service.BancaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bancas")
public class BancaController {

    @Autowired
    private BancaService bancaService;

    @GetMapping
    public List<BancaDto> getAllBancas() {
        return bancaService.findAll();
    }

    @GetMapping("/{id}")
    public BancaDto getBancaById(@PathVariable Long id) {
        return bancaService.findById(id);
    }

    @PostMapping
    public BancaDto createBanca(@RequestBody BancaDto bancaDto) {
        return bancaService.save(bancaDto);
    }

    @PutMapping("/{id}")
    public BancaDto updateBanca(@PathVariable Long id, @RequestBody BancaDto bancaDto) {
        bancaDto.setId(id);
        return bancaService.save(bancaDto);
    }

    @DeleteMapping("/{id}")
    public void deleteBanca(@PathVariable Long id) {
        bancaService.deleteById(id);
    }
}
