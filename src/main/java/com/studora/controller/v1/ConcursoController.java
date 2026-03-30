package com.studora.controller.v1;

import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoFilter;
import com.studora.dto.PageResponse;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.ConcursoService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springdoc.core.annotations.ParameterObject;
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
        description = "Retorna uma página com todos os concursos cadastrados, permitindo filtragem por banca, instituição, cargo, área e status de inscrição.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de concursos retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"instituicao\": {\"id\": 1, \"nome\": \"PF\", \"area\": \"Policial\"}, \"banca\": {\"id\": 1, \"nome\": \"Cebraspe\"}, \"ano\": 2024, \"mes\": 5, \"edital\": \"https://exemplo.com/edital.pdf\", \"cargos\": [{\"id\": 1, \"cargoId\": 1, \"cargoNome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\", \"inscrito\": true, \"topicos\": [{\"id\": 1, \"temaId\": 1, \"temaNome\": \"Poderes\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Administrativo\", \"nome\": \"Atos Administrativos\", \"totalEstudos\": 2, \"ultimoEstudo\": \"2026-01-15T10:30:00\"}]}]}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/concursos\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<ConcursoSummaryDto>> getAllConcursos(
            @ParameterObject @Valid ConcursoFilter filter,
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
        Page<ConcursoSummaryDto> concursos = concursoService.findAll(filter, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(concursos));
    }

    @Operation(
        summary = "Obter concurso por ID",
        description = "Retorna um concurso específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Concurso encontrado", 
                content = @Content(
                    schema = @Schema(implementation = ConcursoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"instituicao\": {\"id\": 1, \"nome\": \"Polícia Federal\", \"area\": \"Policial\"}, \"banca\": {\"id\": 1, \"nome\": \"Cebraspe\"}, \"ano\": 2023, \"mes\": 6, \"edital\": \"Edital 01/2023\", \"cargos\": [{\"id\": 1, \"cargoId\": 1, \"cargoNome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\", \"inscrito\": false, \"topicos\": [{\"id\": 1, \"temaId\": 5, \"temaNome\": \"Poderes\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Administrativo\", \"nome\": \"Atos Administrativos\", \"totalEstudos\": 3, \"ultimoEstudo\": \"2026-02-20T14:00:00\"}, {\"id\": 2, \"temaId\": 5, \"temaNome\": \"Poderes\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Administrativo\", \"nome\": \"Atos Vinculados\", \"totalEstudos\": 0, \"ultimoEstudo\": null}]}]}")
                )),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '123'\",\"instance\":\"/api/v1/concursos/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ConcursoDetailDto> getConcursoById(@PathVariable Long id) {
        return ResponseEntity.ok(concursoService.getConcursoDetailById(id));
    }

    @Operation(
        summary = "Criar novo concurso",
        responses = {
            @ApiResponse(responseCode = "201", description = "Concurso criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 2, \"instituicao\": {\"id\": 2, \"nome\": \"Tribunal de Justiça\", \"area\": \"Judiciária\"}, \"banca\": {\"id\": 2, \"nome\": \"FGV\"}, \"ano\": 2024, \"mes\": 1, \"edital\": \"Edital 01/2024\", \"cargos\": [{\"id\": 10, \"cargoId\": 10, \"cargoNome\": \"Analista\", \"nivel\": \"SUPERIOR\", \"area\": \"Judiciária\", \"inscrito\": false, \"topicos\": [{\"id\": 5, \"temaId\": 3, \"temaNome\": \"Poderes\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Administrativo\", \"nome\": \"Atos Administrativos\", \"totalEstudos\": 0, \"ultimoEstudo\": null}]}]}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/concursos\",\"errors\":{\"ano\":\"deve ser maior que 1900\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um concurso com esta combinação de instituição, banca, ano e mês",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um concurso cadastrado para esta instituição, banca, ano e mês.\",\"instance\":\"/api/v1/concursos\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<ConcursoDetailDto> createConcurso(@Valid @RequestBody ConcursoCreateRequest request) {
        return new ResponseEntity<>(concursoService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar concurso",
        responses = {
            @ApiResponse(responseCode = "200", description = "Concurso atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"instituicao\": {\"id\": 1, \"nome\": \"Polícia Federal\", \"area\": \"Policial\"}, \"banca\": {\"id\": 1, \"nome\": \"Cebraspe\"}, \"ano\": 2023, \"mes\": 5, \"edital\": \"https://exemplo.com/edital.pdf\", \"cargos\": [{\"id\": 1, \"cargoId\": 1, \"cargoNome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\", \"inscrito\": false, \"topicos\": [{\"id\": 5, \"temaId\": 3, \"temaNome\": \"Poderes\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Administrativo\", \"nome\": \"Atos Administrativos\", \"totalEstudos\": 0, \"ultimoEstudo\": null}]}]}")
                )),

            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/concursos/1\",\"errors\":{\"ano\":\"deve ser maior que 1900\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/v1/concursos/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um concurso cadastrado com estes dados",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um concurso cadastrado para esta instituição, banca, ano e mês.\",\"instance\":\"/api/v1/concursos/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ConcursoDetailDto> updateConcurso(@PathVariable Long id, @Valid @RequestBody ConcursoUpdateRequest request) {
        return ResponseEntity.ok(concursoService.update(id, request));
    }

    @Operation(
        summary = "Excluir concurso",
        responses = {
            @ApiResponse(responseCode = "204", description = "Concurso excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/v1/concursos/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcurso(@PathVariable Long id) {
        concursoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Alternar status de inscrição em um cargo de um concurso",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status de inscrição alterado com sucesso",
                content = @Content(
                    examples = @ExampleObject(value = "{\"id\": 1, \"cargoId\": 1, \"cargoNome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\", \"inscrito\": true}"))),
            @ApiResponse(responseCode = "422", description = "Já inscrito em outro cargo",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Você já está inscrito em outro cargo para este concurso. Desinscreva-se primeiro.\",\"instance\":\"/api/v1/concursos/cargos/1/inscricao\"}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Associação concurso-cargo não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar ConcursoCargo com ID: '1'\",\"instance\":\"/api/v1/concursos/cargos/1/inscricao\"}"
                    )))
        }
    )
    @PatchMapping("/cargos/{concursoCargoId}/inscricao")
    public ResponseEntity<com.studora.dto.concurso.ConcursoCargoSummaryDto> toggleInscricao(@PathVariable Long concursoCargoId) {
        return ResponseEntity.ok(concursoService.toggleInscricao(concursoCargoId));
    }
}