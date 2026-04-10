package com.studora.controller.v1;

import com.studora.dto.MetricsLevel;
import com.studora.dto.PageResponse;
import com.studora.dto.instituicao.InstituicaoDetailDto;
import com.studora.dto.instituicao.InstituicaoSummaryDto;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.dto.request.InstituicaoUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.InstituicaoService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/instituicoes")
@Tag(name = "Instituições", description = "Endpoints para gerenciamento de instituições")
public class InstituicaoController {

    private final InstituicaoService instituicaoService;

    public InstituicaoController(InstituicaoService instituicaoService) {
        this.instituicaoService = instituicaoService;
    }

    @Operation(
        summary = "Obter todas as instituições",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de instituições retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Polícia Federal\", \"area\": \"Policial\"}, {\"id\": 2, \"nome\": \"Tribunal de Justiça de São Paulo\", \"area\": \"Judiciária\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "summary",
                            summary = "Acurácia por instituição",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Polícia Federal\", \"area\": \"Policial\", \"questaoStats\": {\"total\": {\"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17, \"dificuldade\": {\"FACIL\": {\"total\": 4, \"corretas\": 2}, \"MEDIA\": {\"total\": 13, \"corretas\": 4}}, \"ultimaQuestao\": \"2026-03-31T23:44:47\"}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Polícia Federal\", \"area\": \"Policial\", \"questaoStats\": {\"total\": {\"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17, \"dificuldade\": {\"FACIL\": {\"total\": 4, \"corretas\": 2}}, \"ultimaQuestao\": \"2026-03-31T23:44:47\"}, \"porNivel\": {\"SUPERIOR\": {\"nome\": \"SUPERIOR\", \"respondidas\": 21, \"acertadas\": 18, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}, \"porBanca\": {\"1\": {\"id\": 1, \"nome\": \"Cebraspe (CESPE)\", \"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}, \"porCargo\": {\"1\": {\"id\": 1, \"nome\": \"Agente de Polícia Federal\", \"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}, \"porAreaCargo\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 21, \"acertadas\": 18, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        )
                    }
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/instituicoes\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<InstituicaoSummaryDto>> getAllInstituicoes(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String metrics) {
        
        MetricsLevel metricsLevel = parseMetrics(metrics);
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("area")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<InstituicaoSummaryDto> instituicoes = instituicaoService.findAll(nome, finalPageable, metricsLevel);
        return ResponseEntity.ok(new PageResponse<>(instituicoes));
    }

    @Operation(summary = "Obter instituição por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Instituição encontrada"),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoDetailDto> getInstituicaoById(
            @PathVariable Long id,
            @RequestParam(required = false) String metrics) {
        return ResponseEntity.ok(instituicaoService.getInstituicaoDetailById(id, parseMetrics(metrics)));
    }

    @Operation(summary = "Criar nova instituição")
    @PostMapping
    public ResponseEntity<Void> createInstituicao(@Valid @RequestBody InstituicaoCreateRequest request) {
        instituicaoService.create(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar instituição")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateInstituicao(@PathVariable Long id, @Valid @RequestBody InstituicaoUpdateRequest request) {
        instituicaoService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Excluir instituição")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstituicao(@PathVariable Long id) {
        instituicaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obter todas as áreas de instituições")
    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAllAreas(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(instituicaoService.findAllAreas(search));
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
