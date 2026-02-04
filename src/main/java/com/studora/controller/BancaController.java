package com.studora.controller;

import com.studora.dto.BancaDto;
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
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bancas")
@Tag(name = "Bancas", description = "Endpoints para gerenciamento de bancas organizadoras")
public class BancaController {

    @Autowired
    private BancaService bancaService;

    @Operation(
        summary = "Obter todas as bancas",
        description = "Retorna uma página com todas as bancas organizadoras cadastradas. Suporta paginação, ordenação prioritária e busca por nome.",
        parameters = {
            @Parameter(name = "nome", description = "Filtro para busca por nome (fuzzy)", schema = @Schema(type = "string")),
            @Parameter(name = "page", description = "Número da página (0..N)", schema = @Schema(type = "integer", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER_STR)),
            @Parameter(name = "size", description = "Tamanho da página", schema = @Schema(type = "integer", defaultValue = AppConstants.DEFAULT_PAGE_SIZE_STR)),
            @Parameter(name = "sort", description = "Campo para ordenação primária", schema = @Schema(type = "string", allowableValues = {"nome"}, defaultValue = "nome")),
            @Parameter(name = "direction", description = "Direção da ordenação primária", schema = @Schema(type = "string", allowableValues = {"ASC", "DESC"}, defaultValue = "ASC"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de bancas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\"}, {\"id\": 2, \"nome\": \"FGV\"}], \"pageNumber\": 0, \"pageSize\": " + AppConstants.DEFAULT_PAGE_SIZE + ", \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/bancas\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<BancaDto>> getAllBancas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<BancaDto> bancas = bancaService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(bancas));
    }

    @Operation(
        summary = "Obter banca por ID",
        description = "Retorna uma banca organizadora específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Banca encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = BancaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Banca com ID: '123'\",\"instance\":\"/api/bancas/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/bancas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BancaDto> getBancaById(
            @Parameter(description = "ID da banca a ser buscada", required = true) @PathVariable Long id) {
        BancaDto banca = bancaService.findById(id);
        return ResponseEntity.ok(banca);
    }

    @Operation(
        summary = "Criar nova banca",
        description = "Cria uma nova banca organizadora com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova banca a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BancaCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova banca criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = BancaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 4, \"nome\": \"FCC\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/bancas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma banca com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma banca com o nome 'FCC'\",\"instance\":\"/api/bancas\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/bancas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<BancaDto> createBanca(
            @Valid @RequestBody BancaCreateRequest bancaCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        BancaDto bancaDto = new BancaDto();
        bancaDto.setNome(bancaCreateRequest.getNome());

        BancaDto createdBanca = bancaService.save(bancaDto);
        return new ResponseEntity<>(createdBanca, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar banca",
        description = "Atualiza os dados de uma banca organizadora existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da banca",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BancaUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Banca atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = BancaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Cebraspe (CESPE) - Atualizada\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/bancas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Banca com ID: '1'\",\"instance\":\"/api/bancas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma banca com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma banca com o nome 'Cebraspe (CESPE) - Atualizada'\",\"instance\":\"/api/bancas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/bancas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<BancaDto> updateBanca(
            @Parameter(description = "ID da banca a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody BancaUpdateRequest bancaUpdateRequest) {
        BancaDto existingBanca = bancaService.findById(id);
        existingBanca.setNome(bancaUpdateRequest.getNome());
        BancaDto updatedBanca = bancaService.save(existingBanca);
        return ResponseEntity.ok(updatedBanca);
    }

    @Operation(
        summary = "Excluir banca",
        description = "Remove uma banca organizadora existente com base no ID fornecido. A exclusão será impedida se houver concursos associados a esta banca.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Banca excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Banca com ID: '1'\",\"instance\":\"/api/bancas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem concursos vinculados a esta banca",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir a banca pois existem concursos associados a ela.\",\"instance\":\"/api/bancas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/bancas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanca(
            @Parameter(description = "ID da banca a ser excluída", required = true) @PathVariable Long id) {
        bancaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
