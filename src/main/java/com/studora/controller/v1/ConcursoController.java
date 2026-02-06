package com.studora.controller.v1;

import com.studora.dto.concurso.ConcursoCargoDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.ConcursoCargoCreateRequest;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.ConcursoService;
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
@RequestMapping("/concursos")
@Tag(name = "Concursos", description = "Endpoints para gerenciamento de concursos")
public class ConcursoController {

    private final ConcursoService concursoService;

    public ConcursoController(ConcursoService concursoService) {
        this.concursoService = concursoService;
    }

    @Operation(
        summary = "Obter todos os concursos",
        description = "Retorna uma página com todos os concursos cadastrados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de concursos retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<ConcursoSummaryDto>> getAllConcursos(
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "ano") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Map<String, String> mapping = Map.of(
            "instituicao", "instituicao.nome",
            "banca", "banca.nome"
        );

        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.desc("ano"),
            Sort.Order.desc("mes"),
            Sort.Order.asc("instituicao.nome"),
            Sort.Order.asc("banca.nome")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, mapping, tieBreakers);
        Page<ConcursoSummaryDto> concursos = concursoService.findAll(finalPageable);
        return ResponseEntity.ok(new PageResponse<>(concursos));
    }

    @Operation(
        summary = "Obter concurso por ID",
        description = "Retorna um concurso específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Concurso encontrado", content = @Content(schema = @Schema(implementation = ConcursoDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ConcursoDetailDto> getConcursoById(@PathVariable Long id) {
        return ResponseEntity.ok(concursoService.getConcursoDetailById(id));
    }

    @Operation(summary = "Criar novo concurso")
    @PostMapping
    public ResponseEntity<ConcursoDetailDto> createConcurso(@Valid @RequestBody ConcursoCreateRequest request) {
        return new ResponseEntity<>(concursoService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar concurso")
    @PutMapping("/{id}")
    public ResponseEntity<ConcursoDetailDto> updateConcurso(@PathVariable Long id, @Valid @RequestBody ConcursoUpdateRequest request) {
        return ResponseEntity.ok(concursoService.update(id, request));
    }

    @Operation(summary = "Excluir concurso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcurso(@PathVariable Long id) {
        concursoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obter cargos por concurso")
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<ConcursoCargoDto>> getCargosByConcurso(@PathVariable Long id) {
        return ResponseEntity.ok(concursoService.getCargosByConcursoId(id));
    }

    @Operation(summary = "Adicionar cargo ao concurso")
    @PostMapping("/{id}/cargos")
    public ResponseEntity<ConcursoCargoDto> addCargoToConcurso(@PathVariable Long id, @Valid @RequestBody ConcursoCargoCreateRequest request) {
        return new ResponseEntity<>(concursoService.addCargoToConcurso(id, request), HttpStatus.CREATED);
    }

    @Operation(summary = "Remover cargo do concurso")
    @DeleteMapping("/{concursoId}/cargos/{cargoId}")
    public ResponseEntity<Void> removeCargoFromConcurso(@PathVariable Long concursoId, @PathVariable Long cargoId) {
        concursoService.removeCargoFromConcurso(concursoId, cargoId);
        return ResponseEntity.noContent().build();
    }
}