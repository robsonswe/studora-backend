package com.studora.controller.v1;

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
@RequestMapping("/bancas")
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
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\"}, {\"id\": 2, \"nome\": \"FGV\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/bancas\"}"
                    )))
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
            @ApiResponse(responseCode = "200", description = "Banca encontrada", 
                content = @Content(
                    schema = @Schema(implementation = BancaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\"}")
                )),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Banca com ID: '123'\",\"instance\":\"/api/v1/bancas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BancaDetailDto> getBancaById(@PathVariable Long id) {
        return ResponseEntity.ok(bancaService.getBancaDetailById(id));
    }

    @Operation(
        summary = "Criar nova banca",
        responses = {
            @ApiResponse(responseCode = "201", description = "Banca criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = BancaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 4, \"nome\": \"FCC\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/bancas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma banca com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma banca com o nome 'FCC'\",\"instance\":\"/api/v1/bancas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<BancaDetailDto> createBanca(@Valid @RequestBody BancaCreateRequest request) {
        return new ResponseEntity<>(bancaService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar banca",
        responses = {
            @ApiResponse(responseCode = "200", description = "Banca atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = BancaDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Cebraspe (CESPE) - Atualizada\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/bancas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Banca com ID: '1'\",\"instance\":\"/api/v1/bancas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Nome já em uso por outra banca",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma banca com o nome 'Cebraspe (CESPE) - Atualizada'\",\"instance\":\"/api/v1/bancas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<BancaDetailDto> updateBanca(@PathVariable Long id, @Valid @RequestBody BancaUpdateRequest request) {
        return ResponseEntity.ok(bancaService.update(id, request));
    }

    @Operation(
        summary = "Excluir banca",
        responses = {
            @ApiResponse(responseCode = "204", description = "Banca excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Banca com ID: '1'\",\"instance\":\"/api/v1/bancas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem concursos vinculados a esta banca",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir a banca pois existem concursos associados a ela.\",\"instance\":\"/api/v1/bancas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanca(@PathVariable Long id) {
        bancaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
