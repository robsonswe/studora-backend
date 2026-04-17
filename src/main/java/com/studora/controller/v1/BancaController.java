package com.studora.controller.v1;

import com.studora.dto.MetricsLevel;
import com.studora.dto.PostResponseDto;
import com.studora.dto.PageResponse;
import com.studora.dto.banca.BancaDetailDto;
import com.studora.dto.banca.BancaSummaryDto;
import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.BancaService;
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
@RequestMapping("/bancas")
@Tag(name = "Bancas", description = "Endpoints para gerenciamento de bancas organizadoras")
public class BancaController {

    private final BancaService bancaService;

    public BancaController(BancaService bancaService) {
        this.bancaService = bancaService;
    }

    @Operation(
        summary = "Obter todas as bancas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de bancas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "lean (padrão)",
                            summary = "Estrutura apenas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\"}, {\"id\": 2, \"nome\": \"FGV\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "summary",
                            summary = "Acurácia por banca",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\", \"questaoStats\": {\"total\": {\"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17, \"dificuldade\": {\"FACIL\": {\"total\": 4, \"corretas\": 2}, \"MEDIA\": {\"total\": 13, \"corretas\": 4}}, \"ultimaQuestao\": \"2026-03-31T23:44:47\"}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "full",
                            summary = "Todas as métricas",
                            value = "{\"content\": [{\"id\": 1, \"nome\": \"Cebraspe (CESPE)\", \"questaoStats\": {\"total\": {\"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17, \"dificuldade\": {\"FACIL\": {\"total\": 4, \"corretas\": 2}, \"MEDIA\": {\"total\": 13, \"corretas\": 4}}, \"ultimaQuestao\": \"2026-03-31T23:44:47\"}, \"porNivel\": {\"SUPERIOR\": {\"nome\": \"SUPERIOR\", \"respondidas\": 21, \"acertadas\": 18, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}, \"porAreaInstituicao\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 21, \"acertadas\": 9, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}, \"porAreaCargo\": {\"Policial\": {\"nome\": \"Policial\", \"respondidas\": 21, \"acertadas\": 18, \"totalQuestoes\": 32, \"mediaTempoResposta\": 17}}}}}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        )
                    }
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
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String metrics) {
        
        MetricsLevel metricsLevel = parseMetrics(metrics);
        List<Sort.Order> tieBreakers = List.of(Sort.Order.asc("nome"));
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<BancaSummaryDto> bancas = bancaService.findAll(nome, finalPageable, metricsLevel);
        return ResponseEntity.ok(new PageResponse<>(bancas));
    }

    @Operation(summary = "Obter banca por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Banca encontrada"),
            @ApiResponse(responseCode = "404", description = "Banca não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BancaDetailDto> getBancaById(
            @PathVariable Long id,
            @RequestParam(required = false) String metrics) {
        return ResponseEntity.ok(bancaService.getBancaDetailById(id, parseMetrics(metrics)));
    }

    @Operation(summary = "Criar nova banca", responses = {
            @ApiResponse(responseCode = "201", description = "Banca criada com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PostResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<PostResponseDto> createBanca(@Valid @RequestBody BancaCreateRequest request) {
        Long id = bancaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PostResponseDto.builder()
                        .id(id)
                        .message("Banca criada com sucesso")
                        .build());
    }

    @Operation(summary = "Atualizar banca")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBanca(@PathVariable Long id, @Valid @RequestBody BancaUpdateRequest request) {
        bancaService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Excluir banca")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanca(@PathVariable Long id) {
        bancaService.delete(id);
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
