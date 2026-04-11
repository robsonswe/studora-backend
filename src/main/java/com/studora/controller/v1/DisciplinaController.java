package com.studora.controller.v1;

import com.studora.dto.MetricsLevel;
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
        description = "Retorna uma página com todas as disciplinas cadastradas. Use o parâmetro `metrics` para incluir dados de desempenho.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de disciplinas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direito Constitucional\"}, {\"id\": 2, \"nome\": \"Direito Administrativo\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "summary",
                            summary = "Progresso + acurácia",
                            value = "{\"content\": [{\"id\": 2, \"nome\": \"Direito Administrativo\", \"ultimoEstudo\": \"2026-03-31T09:40:31\", \"totalTemas\": 1, \"totalSubtemas\": 1, \"subtemasEstudados\": 1, \"temasEstudados\": 1, \"questaoStats\": {\"total\": {\"respondidas\": 21, \"acertadas\": 8, \"totalQuestoes\": 32, \"mediaTempoResposta\": 15, \"dificuldade\": {\"FACIL\": {\"total\": 4, \"corretas\": 2}, \"MEDIA\": {\"total\": 12, \"corretas\": 3}, \"DIFICIL\": {\"total\": 2, \"corretas\": 1}, \"CHUTE\": {\"total\": 3, \"corretas\": 1}}, \"ultimaQuestao\": \"2026-03-31T23:44:47\"}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"content\": [{\"id\": 2, \"nome\": \"Direito Administrativo\", \"ultimoEstudo\": \"2026-03-31T09:40:31\", \"totalTemas\": 1, \"totalSubtemas\": 1, \"subtemasEstudados\": 1, \"temasEstudados\": 1, \"questaoStats\": {\"total\": {\"respondidas\": 21, \"acertadas\": 8, \"totalQuestoes\": 32, \"mediaTempoResposta\": 15, \"dificuldade\": {\"FACIL\": {\"total\": 4, \"corretas\": 2}, \"MEDIA\": {\"total\": 12, \"corretas\": 3}}, \"ultimaQuestao\": \"2026-03-31T23:44:47\"}, \"porNivel\": {\"SUPERIOR\": {\"nome\": \"SUPERIOR\", \"respondidas\": 21, \"acertadas\": 8, \"totalQuestoes\": 32, \"mediaTempoResposta\": 15}}, \"porBanca\": {\"1\": {\"id\": 1, \"nome\": \"Cebraspe (CESPE)\", \"respondidas\": 20, \"acertadas\": 8, \"totalQuestoes\": 31, \"mediaTempoResposta\": 15}}, \"porInstituicao\": {\"1\": {\"id\": 1, \"nome\": \"Polícia Federal\", \"respondidas\": 20, \"acertadas\": 8, \"totalQuestoes\": 31, \"mediaTempoResposta\": 15}}, \"porAreaInstituicao\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 20, \"acertadas\": 8, \"totalQuestoes\": 31, \"mediaTempoResposta\": 15}}, \"porCargo\": {\"1\": {\"id\": 1, \"nome\": \"Agente de Polícia Federal\", \"respondidas\": 20, \"acertadas\": 8, \"totalQuestoes\": 31, \"mediaTempoResposta\": 15}}, \"porAreaCargo\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 20, \"acertadas\": 8, \"totalQuestoes\": 31, \"mediaTempoResposta\": 15}}}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        )
                    }
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
            @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Nível de métricas: summary (progresso+acurácia), full (+tempo+dificuldade). Padrão: lean (estrutura apenas).")
            @RequestParam(required = false) String metrics) {

        MetricsLevel metricsLevel = parseMetrics(metrics);

        List<Sort.Order> tieBreakers = List.of(Sort.Order.asc("nome"));
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);

        Page<DisciplinaSummaryDto> disciplinas = disciplinaService.findAll(nome, finalPageable, metricsLevel);
        return ResponseEntity.ok(new PageResponse<>(disciplinas));
    }

    @Operation(
        summary = "Obter disciplina completa com temas e subtemas",
        description = "Retorna toda a hierarquia (disciplina → temas → subtemas) em uma única resposta. Padrão: métricas completas (full).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Hierarquia completa retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDetailDto.class)
                )),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '123'\",\"instance\":\"/api/v1/disciplinas/123/completo\"}"
                    )))
        }
    )
    @GetMapping("/{id}/completo")
    public ResponseEntity<DisciplinaDetailDto> getDisciplinaCompleto(
            @PathVariable Long id,
            @Parameter(description = "Nível de métricas: full (todos os dados). Padrão: full.")
            @RequestParam(required = false, defaultValue = "full") String metrics) {
        return ResponseEntity.ok(disciplinaService.getDisciplinaCompleto(id, parseMetrics(metrics)));
    }

    @Operation(
        summary = "Obter disciplina por ID",
        description = "Retorna uma disciplina específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disciplina encontrada",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas, temas sem métricas",
                            value = "{\"id\": 1, \"nome\": \"Direito Constitucional\", \"temas\": [{\"id\": 1, \"nome\": \"Remédios Constitucionais\"}]}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas, temas completos",
                            value = "{\"id\": 1, \"nome\": \"Direito Constitucional\", \"totalEstudos\": 10, \"ultimoEstudo\": \"2026-02-20T14:00:00\", \"ultimaQuestao\": \"2026-02-21T10:00:00\", \"totalTemas\": 5, \"totalSubtemas\": 12, \"temasEstudados\": 2, \"subtemasEstudados\": 8, \"totalQuestoes\": 100, \"questoesRespondidas\": 50, \"questoesAcertadas\": 40, \"mediaTempoResposta\": 45, \"dificuldadeRespostas\": {\"FACIL\": {\"total\": 20, \"corretas\": 18}, \"MEDIA\": {\"total\": 30, \"corretas\": 22}}, \"temas\": [{\"id\": 1, \"nome\": \"Remédios Constitucionais\", \"totalSubtemas\": 3, \"subtemasEstudados\": 2, \"questoesRespondidas\": 20, \"questoesAcertadas\": 15, \"mediaTempoResposta\": 38, \"dificuldadeRespostas\": {}}]}"
                        )
                    }
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
    public ResponseEntity<DisciplinaDetailDto> getDisciplinaById(
            @PathVariable Long id,
            @Parameter(description = "Nível de métricas: full (todos os dados). Padrão: lean (estrutura apenas).")
            @RequestParam(required = false) String metrics) {
        return ResponseEntity.ok(disciplinaService.getDisciplinaDetailById(id, parseMetrics(metrics)));
    }

    @Operation(
        summary = "Criar nova disciplina",
        responses = {
            @ApiResponse(responseCode = "201", description = "Disciplina criada com sucesso"),
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
    public ResponseEntity<Void> createDisciplina(@Valid @RequestBody DisciplinaCreateRequest request) {
        disciplinaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Atualizar disciplina",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disciplina atualizada com sucesso"),
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
    public ResponseEntity<Void> updateDisciplina(@PathVariable Long id, @Valid @RequestBody DisciplinaUpdateRequest request) {
        disciplinaService.update(id, request);
        return ResponseEntity.ok().build();
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
