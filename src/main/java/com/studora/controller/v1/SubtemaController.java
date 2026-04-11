package com.studora.controller.v1;

import com.studora.dto.MetricsLevel;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.SubtemaService;
import com.studora.service.EstudoSubtemaService;
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
    private final EstudoSubtemaService estudoSubtemaService;

    public SubtemaController(SubtemaService subtemaService, EstudoSubtemaService estudoSubtemaService) {
        this.subtemaService = subtemaService;
        this.estudoSubtemaService = estudoSubtemaService;
    }

    @Operation(
        summary = "Obter todos os subtemas",
        description = "Retorna uma página com todos os subtemas cadastrados. Use o parâmetro `metrics` para incluir dados de desempenho, e `temaIds` ou `disciplinaIds` para filtrar.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de subtemas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direito à Vida\", \"temaId\": 1, \"temaNome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "summary",
                            summary = "Progresso + acurácia",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direito à Vida\", \"temaId\": 1, \"temaNome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\", \"totalEstudos\": 0, \"questaoStats\": {\"total\": {\"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30, \"dificuldade\": {\"MEDIA\": {\"total\": 1, \"corretas\": 1}}, \"ultimaQuestao\": \"2026-03-31T23:37:57\"}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direito à Vida\", \"temaId\": 1, \"temaNome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\", \"totalEstudos\": 0, \"questaoStats\": {\"total\": {\"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30, \"dificuldade\": {\"MEDIA\": {\"total\": 1, \"corretas\": 1}}, \"ultimaQuestao\": \"2026-03-31T23:37:57\"}, \"porNivel\": {\"SUPERIOR\": {\"nome\": \"SUPERIOR\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porBanca\": {\"1\": {\"id\": 1, \"nome\": \"Cebraspe (CESPE)\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porInstituicao\": {\"1\": {\"id\": 1, \"nome\": \"Polícia Federal\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porAreaInstituicao\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porCargo\": {\"1\": {\"id\": 1, \"nome\": \"Agente de Polícia Federal\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porAreaCargo\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        )
                    }
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
            @RequestParam(required = false) List<Long> temaIds,
            @RequestParam(required = false) List<Long> disciplinaIds,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Nível de métricas: summary (progresso+acurácia), full (+tempo+dificuldade). Padrão: lean (estrutura apenas).")
            @RequestParam(required = false) String metrics) {

        MetricsLevel metricsLevel = parseMetrics(metrics);

        Map<String, String> propertyMapping = Map.of(
            "temaId", "tema.id",
            "temaNome", "tema.nome",
            "disciplinaId", "tema.disciplina.id",
            "disciplinaNome", "tema.disciplina.nome"
        );

        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("tema.id")
        );
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, propertyMapping, tieBreakers);

        Page<SubtemaSummaryDto> subtemas = subtemaService.findAll(nome, temaIds, disciplinaIds, finalPageable, metricsLevel);
        return ResponseEntity.ok(new PageResponse<>(subtemas));
    }

    @Operation(
        summary = "Obter subtema por ID",
        description = "Retorna um subtema específico. Use `metrics=full` para incluir dados de desempenho.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema encontrado",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"id\": 1, \"nome\": \"Habeas Corpus\", \"tema\": {\"id\": 1, \"nome\": \"Remédios Constitucionais\", \"disciplinaNome\": \"Direito Constitucional\"}}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"id\": 1, \"nome\": \"Habeas Corpus\", \"tema\": {\"id\": 1, \"nome\": \"Remédios Constitucionais\"}, \"totalEstudos\": 5, \"ultimoEstudo\": \"2026-02-20T14:00:00\", \"ultimaQuestao\": \"2026-02-21T10:00:00\", \"totalQuestoes\": 10, \"questoesRespondidas\": 8, \"questoesAcertadas\": 6, \"mediaTempoResposta\": 38, \"dificuldadeRespostas\": {\"FACIL\": {\"total\": 3, \"corretas\": 3}, \"MEDIA\": {\"total\": 5, \"corretas\": 3}}}"
                        )
                    }
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
    public ResponseEntity<SubtemaDetailDto> getSubtemaById(
            @PathVariable Long id,
            @Parameter(description = "Nível de métricas: full (todos os dados). Padrão: lean (estrutura apenas).")
            @RequestParam(required = false) String metrics) {
        return ResponseEntity.ok(subtemaService.getSubtemaDetailById(id, parseMetrics(metrics)));
    }

    @Operation(
        summary = "Criar novo subtema",
        responses = {
            @ApiResponse(responseCode = "201", description = "Subtema criado com sucesso"),
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
    public ResponseEntity<Void> createSubtema(@Valid @RequestBody SubtemaCreateRequest request) {
        subtemaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Atualizar subtema",
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/subtemas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '1'\",\"instance\":\"/api/v1/subtemas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um subtema com este nome no mesmo tema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um subtema com o nome 'Direito à Vida' no tema com ID: 1\",\"instance\":\"/api/v1/subtemas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSubtema(@PathVariable Long id, @Valid @RequestBody SubtemaUpdateRequest request) {
        subtemaService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Excluir subtema",
        responses = {
            @ApiResponse(responseCode = "204", description = "Subtema excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '1'\",\"instance\":\"/api/v1/subtemas/1\"}"
                    ))),
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

    @Operation(
        summary = "Adicionar uma sessão de estudo para o subtema",
        responses = {
            @ApiResponse(responseCode = "201", description = "Sessão de estudo registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '1'\",\"instance\":\"/api/v1/subtemas/1/estudos\"}"
                    )))
        }
    )
    @PostMapping("/{id}/estudos")
    public ResponseEntity<com.studora.dto.subtema.EstudoSubtemaDto> addEstudo(@PathVariable Long id) {
        return new ResponseEntity<>(estudoSubtemaService.markAsStudied(id), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Listar sessões de estudo de um subtema",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de sessões de estudo retornada com sucesso")
        }
    )
    @GetMapping("/{id}/estudos")
    public ResponseEntity<List<com.studora.dto.subtema.EstudoSubtemaDto>> getEstudos(@PathVariable Long id) {
        return ResponseEntity.ok(estudoSubtemaService.getEstudosBySubtema(id));
    }

    @Operation(
        summary = "Excluir uma sessão de estudo específica",
        responses = {
            @ApiResponse(responseCode = "204", description = "Sessão de estudo excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sessão de estudo não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Estudo com ID: '1'\",\"instance\":\"/api/v1/subtemas/1/estudos/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{subtemaId}/estudos/{estudoId}")
    public ResponseEntity<Void> deleteEstudo(@PathVariable Long subtemaId, @PathVariable Long estudoId) {
        estudoSubtemaService.deleteEstudo(estudoId);
        return ResponseEntity.noContent().build();
    }

    private MetricsLevel parseMetrics(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return switch (raw.toLowerCase()) {
            case "lean" -> null;
            case "summary" -> MetricsLevel.SUMMARY;
            case "full" -> MetricsLevel.FULL;
            default -> throw new IllegalArgumentException("Invalid metrics level: '" + raw + "'. Valid values: lean, summary, full");
        };
    }
}
