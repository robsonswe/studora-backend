package com.studora.controller.v1;

import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.TemaService;
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
@RequestMapping("/temas")
@Tag(name = "Temas", description = "Endpoints para gerenciamento de temas")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @Operation(
        summary = "Obter todos os temas",
        description = "Retorna uma página com todos os temas cadastrados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de temas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<TemaSummaryDto>> getAllTemas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("disciplina_id")
        );
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<TemaSummaryDto> temas = temaService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(temas));
    }

    @Operation(
        summary = "Obter tema por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema encontrado", 
                content = @Content(
                    schema = @Schema(implementation = TemaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}")
                )),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TemaDetailDto> getTemaById(@PathVariable Long id) {
        return ResponseEntity.ok(temaService.getTemaDetailById(id));
    }

    @Operation(
        summary = "Obter temas por disciplina",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<TemaSummaryDto>> getTemasByDisciplina(@PathVariable Long disciplinaId) {
        return ResponseEntity.ok(temaService.findByDisciplinaId(disciplinaId));
    }

    @Operation(
        summary = "Criar novo tema",
        responses = {
            @ApiResponse(responseCode = "201", description = "Tema criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 2, \"nome\": \"Organização do Estado\", \"disciplinaId\": 1}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/temas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um tema com este nome na mesma disciplina")
        }
    )
    @PostMapping
    public ResponseEntity<TemaDetailDto> createTema(@Valid @RequestBody TemaCreateRequest request) {
        return new ResponseEntity<>(temaService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar tema",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais Atualizado\", \"disciplinaId\": 1}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/temas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um tema com este nome na mesma disciplina")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TemaDetailDto> updateTema(@PathVariable Long id, @Valid @RequestBody TemaUpdateRequest request) {
        return ResponseEntity.ok(temaService.update(id, request));
    }

    @Operation(
        summary = "Excluir tema",
        responses = {
            @ApiResponse(responseCode = "204", description = "Tema excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado"),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Existem subtemas vinculados a este tema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não é possível excluir um tema que possui subtemas associados\",\"instance\":\"/api/v1/temas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTema(@PathVariable Long id) {
        temaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
