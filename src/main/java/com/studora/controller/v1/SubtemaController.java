package com.studora.controller.v1;

import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.SubtemaService;
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
@RequestMapping("/subtemas")
@Tag(name = "Subtemas", description = "Endpoints para gerenciamento de subtemas")
public class SubtemaController {

    private final SubtemaService subtemaService;

    public SubtemaController(SubtemaService subtemaService) {
        this.subtemaService = subtemaService;
    }

    @Operation(summary = "Obter todos os subtemas")
    @GetMapping
    public ResponseEntity<PageResponse<SubtemaSummaryDto>> getAllSubtemas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("tema_id")
        );
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        
        Page<SubtemaSummaryDto> subtemas = subtemaService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(subtemas));
    }

    @Operation(summary = "Obter subtema por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SubtemaDetailDto> getSubtemaById(@PathVariable Long id) {
        return ResponseEntity.ok(subtemaService.getSubtemaDetailById(id));
    }

    @Operation(summary = "Criar novo subtema")
    @PostMapping
    public ResponseEntity<SubtemaDetailDto> createSubtema(@Valid @RequestBody SubtemaCreateRequest request) {
        return new ResponseEntity<>(subtemaService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar subtema")
    @PutMapping("/{id}")
    public ResponseEntity<SubtemaDetailDto> updateSubtema(@PathVariable Long id, @Valid @RequestBody SubtemaUpdateRequest request) {
        return ResponseEntity.ok(subtemaService.update(id, request));
    }

    @Operation(summary = "Excluir subtema")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtema(@PathVariable Long id) {
        subtemaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}