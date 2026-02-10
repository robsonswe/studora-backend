package com.studora.controller.v1;

import com.studora.dto.analytics.ConsistencyDto;
import com.studora.dto.analytics.EvolutionDto;
import com.studora.dto.analytics.LearningRateDto;
import com.studora.dto.analytics.TopicMasteryDto;
import com.studora.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Endpoints para análise de desempenho e progresso")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
        summary = "Obter métricas de consistência diária",
        description = "Retorna um histórico diário de questões respondidas, acertos, tempo gasto e sequências (streaks).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Métricas de consistência retornadas com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "[{\"date\": \"2026-02-10\", \"totalAnswered\": 15, \"totalCorrect\": 12, \"totalTimeSeconds\": 1800, \"activeStreak\": 5}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/analytics/consistencia\"}"
                    )))
        }
    )
    @GetMapping("/consistencia")
    public ResponseEntity<List<ConsistencyDto>> getConsistencia(
            @Parameter(description = "Número de dias retroativos para análise")
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getConsistencia(days));
    }

    @Operation(
        summary = "Obter domínio por disciplinas",
        description = "Retorna uma lista de disciplinas que possuem pelo menos uma questão respondida, com suas respectivas estatísticas de domínio.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de domínio por disciplinas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direito Administrativo\", \"totalAttempts\": 50, \"correctAttempts\": 40, \"avgTimeSeconds\": 45.5, \"difficultyDistribution\": {\"FACIL\": 0.8, \"MEDIA\": 0.7, \"DIFICIL\": 0.5}, \"masteryScore\": 80.0}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/analytics/disciplinas\"}"
                    )))
        }
    )
    @GetMapping("/disciplinas")
    public ResponseEntity<List<TopicMasteryDto>> getDisciplinasMastery() {
        return ResponseEntity.ok(analyticsService.getDisciplinasMastery());
    }

    @Operation(
        summary = "Obter detalhes de domínio de uma disciplina",
        description = "Retorna estatísticas detalhadas de uma disciplina, incluindo seus temas e subtemas (hierarquicamente) que possuem questões respondidas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Detalhes de domínio da disciplina retornados com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direito Administrativo\", \"totalAttempts\": 50, \"correctAttempts\": 40, \"masteryScore\": 80.0, \"children\": [{\"id\": 10, \"nome\": \"Atos Administrativos\", \"totalAttempts\": 20, \"correctAttempts\": 18, \"masteryScore\": 90.0, \"children\": []}]}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/v1/analytics/disciplinas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/analytics/disciplinas/1\"}"
                    )))
        }
    )
    @GetMapping("/disciplinas/{id}")
    public ResponseEntity<TopicMasteryDto> getDisciplinaMasteryDetail(
            @Parameter(description = "ID da disciplina")
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getDisciplinaMasteryDetail(id));
    }

    @Operation(
        summary = "Obter evolução temporal",
        description = "Retorna snapshots semanais da precisão geral, tempo médio de resposta e distribuição de dificuldade.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Evolução temporal retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "[{\"period\": \"2026-W05\", \"overallAccuracy\": 0.75, \"avgResponseTime\": 42, \"difficultyDistribution\": {\"FACIL\": 0.9, \"MEDIA\": 0.6}}] "
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/analytics/evolucao\"}"
                    )))
        }
    )
    @GetMapping("/evolucao")
    public ResponseEntity<List<EvolutionDto>> getEvolucao() {
        return ResponseEntity.ok(analyticsService.getEvolucao());
    }

    @Operation(
        summary = "Obter taxa de aprendizado",
        description = "Analisa o desempenho em questões repetidas, calculando taxas de recuperação (Erro -> Acerto) e retenção (Acerto -> Acerto).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Taxa de aprendizado retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"totalRepeatedQuestions\": 10, \"recoveryRate\": 0.6, \"retentionRate\": 0.9, \"data\": [{\"attemptNumber\": 1, \"accuracy\": 0.4}, {\"attemptNumber\": 2, \"accuracy\": 0.7}]}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/analytics/taxa-aprendizado\"}"
                    )))
        }
    )
    @GetMapping("/taxa-aprendizado")
    public ResponseEntity<LearningRateDto> getTaxaAprendizado() {
        return ResponseEntity.ok(analyticsService.getTaxaAprendizado());
    }
}
