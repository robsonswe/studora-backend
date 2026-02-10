package com.studora.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para taxa de aprendizado em questões repetidas")
public class LearningRateDto {
    @Schema(description = "Total de questões respondidas mais de uma vez")
    private Integer totalRepeatedQuestions;

    @Schema(description = "Taxa de recuperação (% de Erro -> Acerto)")
    private Double recoveryRate;

    @Schema(description = "Taxa de retenção (% de Acerto -> Acerto)")
    private Double retentionRate;

    @Schema(description = "Dados detalhados por número da tentativa")
    private List<AttemptData> data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttemptData {
        private Integer attemptNumber;
        private Double accuracy;
    }
}
