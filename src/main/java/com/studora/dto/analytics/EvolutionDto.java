package com.studora.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para evolução temporal de métricas")
public class EvolutionDto {
    @Schema(description = "Identificador do período (ex: 2026-02-10 ou 2026-W05)")
    private String period;

    @Schema(description = "Precisão geral no período (0.0 a 1.0)")
    private Double overallAccuracy;

    @Schema(description = "Tempo médio de resposta no período em segundos")
    private Integer avgResponseTime;

    @Schema(description = "Distribuição de dificuldade das questões respondidas no período")
    private Map<String, Double> difficultyDistribution;
}
