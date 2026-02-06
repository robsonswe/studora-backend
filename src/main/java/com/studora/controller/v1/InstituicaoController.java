package com.studora.controller.v1;

import com.studora.dto.instituicao.InstituicaoSummaryDto;
import com.studora.dto.instituicao.InstituicaoDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.dto.request.InstituicaoUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.InstituicaoService;
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
@RequestMapping("/instituicoes")
@Tag(name = "Instituições", description = "Endpoints para gerenciamento de instituições")
public class InstituicaoController {

    private final InstituicaoService instituicaoService;

    public InstituicaoController(InstituicaoService instituicaoService) {
        this.instituicaoService = instituicaoService;
    }

    @Operation(
        summary = "Obter todas as instituições",
        description = "Retorna uma página com todas as instituições cadastradas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de instituições retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<InstituicaoSummaryDto>> getAllInstituicoes(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("area")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<InstituicaoSummaryDto> instituicoes = instituicaoService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(instituicoes));
    }

    @Operation(
        summary = "Obter instituição por ID",
        description = "Retorna uma instituição específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Instituição encontrada", content = @Content(schema = @Schema(implementation = InstituicaoDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoDetailDto> getInstituicaoById(@PathVariable Long id) {
        return ResponseEntity.ok(instituicaoService.getInstituicaoDetailById(id));
    }

    @Operation(summary = "Criar nova instituição")
    @PostMapping
    public ResponseEntity<InstituicaoDetailDto> createInstituicao(@RequestBody @Valid InstituicaoCreateRequest request) {
        return new ResponseEntity<>(instituicaoService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar instituição")
    @PutMapping("/{id}")
    public ResponseEntity<InstituicaoDetailDto> updateInstituicao(@PathVariable Long id, @RequestBody @Valid InstituicaoUpdateRequest request) {
        return ResponseEntity.ok(instituicaoService.update(id, request));
    }

    @Operation(summary = "Excluir instituição")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstituicao(@PathVariable Long id) {
        instituicaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obter todas as áreas de instituições")
    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAllAreas(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(instituicaoService.findAllAreas(search));
    }
}