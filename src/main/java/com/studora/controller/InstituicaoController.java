package com.studora.controller;

import com.studora.dto.InstituicaoDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.dto.request.InstituicaoUpdateRequest;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instituicoes")
@Tag(name = "Instituições", description = "Endpoints para gerenciamento de instituições")
public class InstituicaoController {

    @Autowired
    private InstituicaoService instituicaoService;

    @Operation(
        summary = "Obter todas as instituições",
        description = "Retorna uma página com todas as instituições cadastradas. Suporta paginação, ordenação prioritária e busca por nome.",
        parameters = {
            @Parameter(name = "nome", description = "Filtro para busca por nome (fuzzy)", schema = @Schema(type = "string")),
            @Parameter(name = "page", description = "Número da página (0..N)", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "Tamanho da página", schema = @Schema(type = "integer", defaultValue = "20")),
            @Parameter(name = "sort", description = "Campo para ordenação primária", schema = @Schema(type = "string", allowableValues = {"nome", "area"}, defaultValue = "nome")),
            @Parameter(name = "direction", description = "Direção da ordenação primária", schema = @Schema(type = "string", allowableValues = {"ASC", "DESC"}, defaultValue = "ASC"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de instituições retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Tribunal de Justiça de São Paulo\", \"area\": \"Judiciária\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<InstituicaoDto>> getAllInstituicoes(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("area")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<InstituicaoDto> instituicoes = instituicaoService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(instituicoes));
    }

    @Operation(
        summary = "Obter instituição por ID",
        description = "Retorna uma instituição específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Instituição encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Polícia Federal\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '123'\",\"instance\":\"/api/instituicoes/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoDto> getInstituicaoById(
            @Parameter(description = "ID da instituição a ser buscada", required = true) @PathVariable Long id) {
        InstituicaoDto instituicao = instituicaoService.findById(id);
        return ResponseEntity.ok(instituicao);
    }

    @Operation(
        summary = "Criar nova instituição",
        description = "Cria uma nova instituição com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova instituição a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InstituicaoCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova instituição criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"nome\": \"Receita Federal\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/instituicoes\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma instituição com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma instituição com o nome 'Receita Federal'\",\"instance\":\"/api/instituicoes\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<InstituicaoDto> createInstituicao(
            @RequestBody InstituicaoCreateRequest instituicaoCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        InstituicaoDto instituicaoDto = new InstituicaoDto();
        instituicaoDto.setNome(instituicaoCreateRequest.getNome());
        instituicaoDto.setArea(instituicaoCreateRequest.getArea());

        InstituicaoDto createdInstituicao = instituicaoService.save(instituicaoDto);
        return new ResponseEntity<>(createdInstituicao, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar instituição",
        description = "Atualiza os dados de uma instituição existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da instituição",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InstituicaoUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Instituição atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Polícia Federal Atualizada\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/instituicoes/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '1'\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma instituição com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma instituição com o nome 'Polícia Federal Atualizada'\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<InstituicaoDto> updateInstituicao(
            @Parameter(description = "ID da instituição a ser atualizada", required = true) @PathVariable Long id,
            @RequestBody InstituicaoUpdateRequest instituicaoUpdateRequest) {
        // Get the existing institution and update name and area from the request
        InstituicaoDto existingInstituicao = instituicaoService.findById(id);
        existingInstituicao.setNome(instituicaoUpdateRequest.getNome());
        existingInstituicao.setArea(instituicaoUpdateRequest.getArea());
        InstituicaoDto updatedInstituicao = instituicaoService.save(existingInstituicao);
        return ResponseEntity.ok(updatedInstituicao);
    }

    @Operation(
        summary = "Excluir instituição",
        description = "Remove uma instituição existente com base no ID fornecido. A exclusão será impedida se houver concursos associados a esta instituição.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Instituição excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '1'\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem concursos vinculados a esta instituição",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir a instituição pois existem concursos associados a ela.\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstituicao(
            @Parameter(description = "ID da institution a ser excluída", required = true) @PathVariable Long id) {
        instituicaoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Obter todas as áreas de instituições",
        description = "Retorna uma lista com todas as áreas únicas cadastradas para as instituições. Suporta busca fuzzy.",
        parameters = {
            @Parameter(name = "search", description = "Filtro para busca por área (fuzzy)", schema = @Schema(type = "string"))
        },
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
