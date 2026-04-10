package com.studora.controller.v1;

import com.studora.dto.MetricsLevel;
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
        description = "Retorna uma página com todos os temas cadastrados. Use o parâmetro `metrics` para incluir dados de desempenho.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de temas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\", \"subtemas\": [{\"id\": 1, \"nome\": \"Direito à Vida\"}, {\"id\": 2, \"nome\": \"Habeas Corpus\"}]}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "summary",
                            summary = "Progresso + acurácia",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\", \"ultimoEstudo\": \"2026-04-02T17:39:33\", \"totalSubtemas\": 2, \"subtemasEstudados\": 1, \"questaoStats\": {\"total\": {\"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30, \"dificuldade\": {\"MEDIA\": {\"total\": 1, \"corretas\": 1}}, \"ultimaQuestao\": \"2026-03-31T23:37:57\"}}, \"subtemas\": [{\"id\": 1, \"nome\": \"Direito à Vida\"}, {\"id\": 2, \"nome\": \"Habeas Corpus\"}]}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\", \"ultimoEstudo\": \"2026-04-02T17:39:33\", \"totalSubtemas\": 2, \"subtemasEstudados\": 1, \"questaoStats\": {\"total\": {\"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30, \"dificuldade\": {\"MEDIA\": {\"total\": 1, \"corretas\": 1}}, \"ultimaQuestao\": \"2026-03-31T23:37:57\"}, \"porNivel\": {\"SUPERIOR\": {\"nome\": \"SUPERIOR\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porBanca\": {\"1\": {\"id\": 1, \"nome\": \"Cebraspe (CESPE)\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porInstituicao\": {\"1\": {\"id\": 1, \"nome\": \"Polícia Federal\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porAreaInstituicao\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porCargo\": {\"1\": {\"id\": 1, \"nome\": \"Agente de Polícia Federal\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}, \"porAreaCargo\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 1, \"acertadas\": 1, \"totalQuestoes\": 1, \"mediaTempoResposta\": 30}}}, \"subtemas\": [{\"id\": 1, \"nome\": \"Direito à Vida\"}, {\"id\": 2, \"nome\": \"Habeas Corpus\"}]}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        )
                    }
                ))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<TemaSummaryDto>> getAllTemas(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Nível de métricas: summary (progresso+acurácia), full (+tempo+dificuldade). Padrão: lean (estrutura apenas).")
            @RequestParam(required = false) String metrics) {

        MetricsLevel metricsLevel = parseMetrics(metrics);

        Map<String, String> propertyMapping = Map.of(
            "disciplinaId", "disciplina.id",
            "disciplinaNome", "disciplina.nome"
        );

        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("disciplina.id")
        );
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, propertyMapping, tieBreakers);
        Page<TemaSummaryDto> temas = temaService.findAll(nome, finalPageable, metricsLevel);
        return ResponseEntity.ok(new PageResponse<>(temas));
    }

    @Operation(
        summary = "Obter tema por ID",
        description = "Retorna um tema específico. Use `metrics=full` para incluir dados de desempenho.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema encontrado",
                content = @Content(
                    schema = @Schema(implementation = TemaDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplina\": {\"id\": 1, \"nome\": \"Direito Constitucional\"}, \"subtemas\": []}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplina\": {\"id\": 1, \"nome\": \"Direito Constitucional\", \"totalEstudos\": 10}, \"subtemas\": [{\"id\": 10, \"nome\": \"Habeas Corpus\"}], \"totalSubtemas\": 3, \"subtemasEstudados\": 2, \"questoesRespondidas\": 20, \"questoesAcertadas\": 15, \"mediaTempoResposta\": 45, \"dificuldadeRespostas\": {\"FACIL\": {\"total\": 10, \"corretas\": 8}}}"
                        )
                    }
                )),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '123'\",\"instance\":\"/api/v1/temas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TemaDetailDto> getTemaById(
            @PathVariable Long id,
            @Parameter(description = "Nível de métricas: full (todos os dados). Padrão: lean (estrutura apenas).")
            @RequestParam(required = false) String metrics) {
        return ResponseEntity.ok(temaService.getTemaDetailById(id, parseMetrics(metrics)));
    }

    @Operation(
        summary = "Obter temas por disciplina",
        description = "Retorna todos os temas de uma disciplina. Use o parâmetro `metrics` para incluir dados de desempenho.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\"}]"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1, \"disciplinaNome\": \"Direito Constitucional\", \"totalSubtemas\": 3, \"subtemasEstudados\": 2, \"questoesRespondidas\": 20, \"questoesAcertadas\": 15, \"mediaTempoResposta\": 45, \"dificuldadeRespostas\": {\"FACIL\": {\"total\": 10, \"corretas\": 8}}}]"
                        )
                    }
                ))
        }
    )
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<TemaSummaryDto>> getTemasByDisciplina(
            @PathVariable Long disciplinaId,
            @Parameter(description = "Nível de métricas: summary, full. Padrão: lean.")
            @RequestParam(required = false) String metrics) {
        return ResponseEntity.ok(temaService.findByDisciplinaId(disciplinaId, parseMetrics(metrics)));
    }

    @Operation(
        summary = "Criar novo tema",
        responses = {
            @ApiResponse(responseCode = "201", description = "Tema criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/temas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um tema com este nome na mesma disciplina",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um tema com o nome 'Direitos Fundamentais' na disciplina com ID: 1\",\"instance\":\"/api/v1/temas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<Void> createTema(@Valid @RequestBody TemaCreateRequest request) {
        temaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Atualizar tema",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/temas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '1'\",\"instance\":\"/api/v1/temas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um tema com este nome na mesma disciplina",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um tema com o nome 'Direitos Fundamentais' na disciplina com ID: 1\",\"instance\":\"/api/v1/temas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTema(@PathVariable Long id, @Valid @RequestBody TemaUpdateRequest request) {
        temaService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Excluir tema",
        responses = {
            @ApiResponse(responseCode = "204", description = "Tema excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '1'\",\"instance\":\"/api/v1/temas/1\"}"
                    ))),
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
