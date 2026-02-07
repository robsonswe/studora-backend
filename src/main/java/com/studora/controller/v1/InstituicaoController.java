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
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Tribunal de Justiça de São Paulo\", \"area\": \"Judiciária\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/instituicoes\"}"
                    )))
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
            @ApiResponse(responseCode = "200", description = "Instituição encontrada", 
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Polícia Federal\"}")
                )),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '123'\",\"instance\":\"/api/v1/instituicoes/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoDetailDto> getInstituicaoById(@PathVariable Long id) {
        return ResponseEntity.ok(instituicaoService.getInstituicaoDetailById(id));
    }

    @Operation(
        summary = "Criar nova instituição",
        responses = {
            @ApiResponse(responseCode = "201", description = "Instituição criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 2, \"nome\": \"Receita Federal\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/instituicoes\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma instituição com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma instituição com o nome 'Receita Federal'\",\"instance\":\"/api/v1/instituicoes\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<InstituicaoDetailDto> createInstituicao(@RequestBody @Valid InstituicaoCreateRequest request) {
        return new ResponseEntity<>(instituicaoService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar instituição",
        responses = {
            @ApiResponse(responseCode = "200", description = "Instituição atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Polícia Federal Atualizada\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/instituicoes/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '1'\",\"instance\":\"/api/v1/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma instituição com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma instituição com o nome 'Polícia Federal Atualizada'\",\"instance\":\"/api/v1/instituicoes/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<InstituicaoDetailDto> updateInstituicao(@PathVariable Long id, @RequestBody @Valid InstituicaoUpdateRequest request) {
        return ResponseEntity.ok(instituicaoService.update(id, request));
    }

    @Operation(
        summary = "Excluir instituição",
        responses = {
            @ApiResponse(responseCode = "204", description = "Instituição excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '1'\",\"instance\":\"/api/v1/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem concursos vinculados a esta instituição",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir a instituição pois existem concursos associados a ela.\",\"instance\":\"/api/v1/instituicoes/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstituicao(@PathVariable Long id) {
        instituicaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Obter todas as áreas de instituições",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de áreas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(type = "string")),
                    examples = @ExampleObject(value = "[\"Educação\", \"Judiciária\", \"Fiscal\"]")
                ))
        }
    )
    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAllAreas(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(instituicaoService.findAllAreas(search));
    }
}
