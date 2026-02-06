package com.studora.controller;

import com.studora.dto.banca.BancaSummaryDto;
import com.studora.dto.banca.BancaDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.BancaService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bancas")
@Tag(name = "Bancas", description = "Endpoints para gerenciamento de bancas organizadoras")
public class BancaController {

    private final BancaService bancaService;

    public BancaController(BancaService bancaService) {
        this.bancaService = bancaService;
    }

    @Operation(
        summary = "Obter todas as bancas",
        description = "Retorna uma página com todas as bancas organizadoras cadastradas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de bancas retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<BancaSummaryDto>> getAllBancas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(Sort.Order.asc("nome"));
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<BancaSummaryDto> bancas = bancaService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(bancas));
    }

    @Operation(
        summary = "Obter banca por ID",
        description = "Retorna uma banca organizadora específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Banca encontrada", content = @Content(schema = @Schema(implementation = BancaDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BancaDetailDto> getBancaById(@PathVariable Long id) {
        return ResponseEntity.ok(bancaService.getBancaDetailById(id));
    }

    @Operation(summary = "Criar nova banca")
    @PostMapping
    public ResponseEntity<BancaDetailDto> createBanca(@Valid @RequestBody BancaCreateRequest request) {
        return new ResponseEntity<>(bancaService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar banca")
    @PutMapping("/{id}")
    public ResponseEntity<BancaDetailDto> updateBanca(@PathVariable Long id, @Valid @RequestBody BancaUpdateRequest request) {
        return ResponseEntity.ok(bancaService.update(id, request));
    }

    @Operation(summary = "Excluir banca")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanca(@PathVariable Long id) {
        bancaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}