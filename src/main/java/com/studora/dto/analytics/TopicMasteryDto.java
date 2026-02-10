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
@Schema(description = "DTO para domínio de tópicos (Disciplina, Tema ou Subtema)")
public class TopicMasteryDto {
    @Schema(description = "ID do tópico")
    private Long id;

    @Schema(description = "Nome do tópico")
    private String nome;

    @Schema(description = "Total de tentativas")
    private Integer totalAttempts;

    @Schema(description = "Total de acertos")
    private Integer correctAttempts;

    @Schema(description = "Tempo médio de resposta em segundos")
    private Integer avgTimeSeconds;

    @Schema(description = "Estatísticas por dificuldade")
    private Map<String, DifficultyStat> difficultyStats;

    @Schema(description = "Total de acertos por chute")
    private Integer guessCount;

    @Schema(description = "Pontuação de domínio (ponderada pela dificuldade)")
    private Double masteryScore;

    @Schema(description = "Tópicos filhos (ex: Temas de uma Disciplina, ou Subtemas de um Tema)")
    private java.util.List<TopicMasteryDto> children;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DifficultyStat {
        private Integer total;
        private Integer correct;
    }
}
