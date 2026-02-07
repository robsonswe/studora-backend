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
@RequestMapping("/subtemas")
@Tag(name = "Subtemas", description = "Endpoints para gerenciamento de subtemas")
public class SubtemaController {

    private final SubtemaService subtemaService;

    public SubtemaController(SubtemaService subtemaService) {
        this.subtemaService = subtemaService;
    }

    @Operation(
        summary = "Obter todos os subtemas",
        description = "Retorna uma página com todos os subtemas cadastrados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de subtemas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Atos Administrativos\", \"temaId\": 5}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/subtemas\"}"
                    )))
        }
    )
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

    @Operation(
        summary = "Obter subtema por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema encontrado", 
                content = @Content(
                    schema = @Schema(implementation = SubtemaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Habeas Corpus\", \"temaId\": 1}")
                )),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '123'\",\"instance\":\"/api/v1/subtemas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SubtemaDetailDto> getSubtemaById(@PathVariable Long id) {
        return ResponseEntity.ok(subtemaService.getSubtemaDetailById(id));
    }

    @Operation(
        summary = "Criar novo subtema",
        responses = {
            @ApiResponse(responseCode = "201", description = "Subtema criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 3, \"nome\": \"Direito à Vida\", \"temaId\": 1}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/subtemas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um subtema com este nome no mesmo tema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um subtema com o nome 'Direito à Vida' no tema com ID: 1\",\"instance\":\"/api/v1/subtemas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<SubtemaDetailDto> createSubtema(@Valid @RequestBody SubtemaCreateRequest request) {
        return new ResponseEntity<>(subtemaService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar subtema",
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Habeas Corpus e Habeas Data\", \"temaId\": 1}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/subtemas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um subtema com este nome no mesmo tema")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<SubtemaDetailDto> updateSubtema(@PathVariable Long id, @Valid @RequestBody SubtemaUpdateRequest request) {
        return ResponseEntity.ok(subtemaService.update(id, request));
    }

    @Operation(
        summary = "Excluir subtema",
        responses = {
            @ApiResponse(responseCode = "204", description = "Subtema excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado"),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Existem questões vinculadas a este subtema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não é possível excluir um subtema que possui questões associadas\",\"instance\":\"/api/v1/subtemas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtema(@PathVariable Long id) {
        subtemaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
