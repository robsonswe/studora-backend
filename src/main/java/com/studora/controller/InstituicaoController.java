package com.studora.controller;

import com.studora.dto.InstituicaoDto;
import com.studora.service.InstituicaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoController {

    @Autowired
    private InstituicaoService instituicaoService;

    @GetMapping
    public List<InstituicaoDto> getAllInstituicoes() {
        return instituicaoService.findAll();
    }

    @GetMapping("/{id}")
    public InstituicaoDto getInstituicaoById(@PathVariable Long id) {
        return instituicaoService.findById(id);
    }

    @PostMapping
    public InstituicaoDto createInstituicao(@RequestBody InstituicaoDto instituicaoDto) {
        return instituicaoService.save(instituicaoDto);
    }

    @PutMapping("/{id}")
    public InstituicaoDto updateInstituicao(@PathVariable Long id, @RequestBody InstituicaoDto instituicaoDto) {
        instituicaoDto.setId(id);
        return instituicaoService.save(instituicaoDto);
    }

    @DeleteMapping("/{id}")
    public void deleteInstituicao(@PathVariable Long id) {
        instituicaoService.deleteById(id);
    }
}
