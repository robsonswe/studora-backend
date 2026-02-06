package com.studora.controller;

import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.DisciplinaService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disciplinas")
@Tag(name = "Disciplinas", description = "Endpoints para gerenciamento de disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @Operation(summary = "Obter todas as disciplinas")
    @GetMapping
    public ResponseEntity<PageResponse<DisciplinaSummaryDto>> getAllDisciplinas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(Sort.Order.asc("nome"));
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        
        Page<DisciplinaSummaryDto> disciplinas = disciplinaService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(disciplinas));
    }

    @Operation(summary = "Obter disciplina por ID")
    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaDetailDto> getDisciplinaById(@PathVariable Long id) {
        return ResponseEntity.ok(disciplinaService.getDisciplinaDetailById(id));
    }

    @Operation(summary = "Criar nova disciplina")
    @PostMapping
    public ResponseEntity<DisciplinaDetailDto> createDisciplina(@Valid @RequestBody DisciplinaCreateRequest request) {
        return new ResponseEntity<>(disciplinaService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar disciplina")
    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaDetailDto> updateDisciplina(@PathVariable Long id, @Valid @RequestBody DisciplinaUpdateRequest request) {
        return ResponseEntity.ok(disciplinaService.update(id, request));
    }

    @Operation(summary = "Excluir disciplina")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisciplina(@PathVariable Long id) {
        disciplinaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}