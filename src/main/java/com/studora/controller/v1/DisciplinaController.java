package com.studora.controller.v1;

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
@RequestMapping("/disciplinas")
@Tag(name = "Disciplinas", description = "Endpoints para gerenciamento de disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @Operation(
        summary = "Obter todas as disciplinas",
        description = "Retorna uma página com todas as disciplinas cadastradas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de disciplinas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Direito Constitucional\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/disciplinas\"}"
                    )))
        }
    )
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

    @Operation(
        summary = "Obter disciplina por ID",
        description = "Retorna uma disciplina específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disciplina encontrada", 
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Direito Constitucional\"}")
                )),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '123'\",\"instance\":\"/api/v1/disciplinas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaDetailDto> getDisciplinaById(@PathVariable Long id) {
        return ResponseEntity.ok(disciplinaService.getDisciplinaDetailById(id));
    }

    @Operation(
        summary = "Criar nova disciplina",
        responses = {
            @ApiResponse(responseCode = "201", description = "Disciplina criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 3, \"nome\": \"Direito Administrativo\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/disciplinas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma disciplina com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma disciplina com o nome 'Direito Administrativo'\",\"instance\":\"/api/v1/disciplinas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<DisciplinaDetailDto> createDisciplina(@Valid @RequestBody DisciplinaCreateRequest request) {
        return new ResponseEntity<>(disciplinaService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar disciplina",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disciplina atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Direito Constitucional Aplicado\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/disciplinas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/v1/disciplinas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma disciplina com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma disciplina com o nome 'Direito Constitucional Aplicado'\",\"instance\":\"/api/v1/disciplinas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaDetailDto> updateDisciplina(@PathVariable Long id, @Valid @RequestBody DisciplinaUpdateRequest request) {
        return ResponseEntity.ok(disciplinaService.update(id, request));
    }

    @Operation(
        summary = "Excluir disciplina",
        responses = {
            @ApiResponse(responseCode = "204", description = "Disciplina excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/v1/disciplinas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem temas vinculados a esta disciplina",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir a disciplina pois existem temas associados a ela.\",\"instance\":\"/api/v1/disciplinas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisciplina(@PathVariable Long id) {
        disciplinaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
