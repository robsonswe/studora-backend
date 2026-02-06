package com.studora.controller.v1;

import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.TemaService;
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
@RequestMapping("/temas")
@Tag(name = "Temas", description = "Endpoints para gerenciamento de temas")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @Operation(summary = "Obter todos os temas")
    @GetMapping
    public ResponseEntity<PageResponse<TemaSummaryDto>> getAllTemas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("disciplina_id")
        );
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        
        Page<TemaSummaryDto> temas = temaService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(temas));
    }

    @Operation(summary = "Obter tema por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TemaDetailDto> getTemaById(@PathVariable Long id) {
        return ResponseEntity.ok(temaService.getTemaDetailById(id));
    }

    @Operation(summary = "Criar novo tema")
    @PostMapping
    public ResponseEntity<TemaDetailDto> createTema(@Valid @RequestBody TemaCreateRequest request) {
        return new ResponseEntity<>(temaService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar tema")
    @PutMapping("/{id}")
    public ResponseEntity<TemaDetailDto> updateTema(@PathVariable Long id, @Valid @RequestBody TemaUpdateRequest request) {
        return ResponseEntity.ok(temaService.update(id, request));
    }

    @Operation(summary = "Excluir tema")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTema(@PathVariable Long id) {
        temaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}