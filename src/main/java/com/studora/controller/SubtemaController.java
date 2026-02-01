package com.studora.controller;

import com.studora.dto.SubtemaDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.service.SubtemaService;
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
@RequestMapping("/api/subtemas")
@CrossOrigin(origins = "*")
@Tag(name = "Subtemas", description = "Endpoints para gerenciamento de subtemas")
public class SubtemaController {

    @Autowired
    private SubtemaService subtemaService;

    @Operation(
        summary = "Obter todos os subtemas",
        description = "Retorna uma página com todos os subtemas cadastrados. Suporta paginação, ordenação prioritária e busca por nome.",
        parameters = {
            @Parameter(name = "nome", description = "Filtro para busca por nome (fuzzy)", schema = @Schema(type = "string")),
            @Parameter(name = "page", description = "Número da página (0..N)", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "Tamanho da página", schema = @Schema(type = "integer", defaultValue = "20")),
            @Parameter(name = "sort", description = "Campo para ordenação primária", schema = @Schema(type = "string", allowableValues = {"nome", "temaId"}, defaultValue = "nome")),
            @Parameter(name = "direction", description = "Direção da ordenação primária", schema = @Schema(type = "string", allowableValues = {"ASC", "DESC"}, defaultValue = "ASC"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de subtemas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Atos Administrativos\", \"temaId\": 5}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/subtemas\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<SubtemaDto>> getAllSubtemas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Map<String, String> mapping = Map.of("temaId", "tema.id");

        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("tema.id")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, mapping, tieBreakers);
        Page<SubtemaDto> subtemas = subtemaService.getAllSubtemas(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(subtemas));
    }

    @Operation(
        summary = "Obter subtema por ID",
        description = "Retorna um subtema específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema encontrado e retornado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Habeas Corpus\", \"temaId\": 1}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '123'\",\"instance\":\"/api/subtemas/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/subtemas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SubtemaDto> getSubtemaById(
            @Parameter(description = "ID do subtema a ser buscada", required = true) @PathVariable Long id) {
        SubtemaDto subtema = subtemaService.getSubtemaById(id);
        return ResponseEntity.ok(subtema);
    }

    @Operation(
        summary = "Obter subtemas por ID do tema",
        description = "Retorna uma página de subtemas associados a um tema específico. Suporta paginação.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de subtemas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Habeas Corpus\", \"temaId\": 1}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '1'\",\"instance\":\"/api/subtemas/tema/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/subtemas/tema/1\"}"
                    )))
        }
    )
    @GetMapping("/tema/{temaId}")
    public ResponseEntity<PageResponse<SubtemaDto>> getSubtemasByTemaId(
            @Parameter(description = "ID do tema para filtrar subtemas", required = true) @PathVariable Long temaId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<SubtemaDto> subtemas = subtemaService.getSubtemasByTemaId(temaId, pageable);
        return ResponseEntity.ok(new PageResponse<>(subtemas));
    }

    @Operation(
        summary = "Criar novo subtema",
        description = "Cria um novo subtema com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo subtema a ser criado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubtemaCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo subtema criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 3, \"nome\": \"Direito à Vida\", \"temaId\": 1}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/subtemas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um subtema com este nome no mesmo tema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um subtema com o nome 'Direito à Vida' no tema com ID: 1\",\"instance\":\"/api/subtemas\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/subtemas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<SubtemaDto> createSubtema(
            @Valid @RequestBody SubtemaCreateRequest subtemaCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        SubtemaDto subtemaDto = new SubtemaDto();
        subtemaDto.setTemaId(subtemaCreateRequest.getTemaId());
        subtemaDto.setNome(subtemaCreateRequest.getNome());

        SubtemaDto createdSubtema = subtemaService.createSubtema(subtemaDto);
        return new ResponseEntity<>(createdSubtema, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar subtema",
        description = "Atualiza os dados de um subtema existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do subtema",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubtemaUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Habeas Corpus e Habeas Data\", \"temaId\": 1}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/subtemas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '1'\",\"instance\":\"/api/subtemas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um subtema com este nome no mesmo tema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um subtema com o nome 'Habeas Corpus e Habeas Data' no tema com ID: 1\",\"instance\":\"/api/subtemas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/subtemas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<SubtemaDto> updateSubtema(
            @Parameter(description = "ID do subtema a ser atualizado", required = true) @PathVariable Long id,
            @Valid @RequestBody SubtemaUpdateRequest subtemaUpdateRequest) {
        // Convert the request DTO to the regular DTO for processing
        SubtemaDto subtemaDto = new SubtemaDto();
        subtemaDto.setId(id); // Set the ID from the path parameter
        subtemaDto.setTemaId(subtemaUpdateRequest.getTemaId());
        subtemaDto.setNome(subtemaUpdateRequest.getNome());

        SubtemaDto updatedSubtema = subtemaService.updateSubtema(id, subtemaDto);
        return ResponseEntity.ok(updatedSubtema);
    }

    @Operation(
        summary = "Excluir subtema",
        description = "Remove um subtema existente com base no ID fornecido. A exclusão será impedida se houver questões associadas a este subtema.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Subtema excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '1'\",\"instance\":\"/api/subtemas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem questões vinculadas a este subtema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir o subtema pois existem questões associadas a ele.\",\"instance\":\"/api/subtemas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/subtemas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtema(
            @Parameter(description = "ID do subtema a ser excluído", required = true) @PathVariable Long id) {
        subtemaService.deleteSubtema(id);
        return ResponseEntity.noContent().build();
    }
}
