package com.studora.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para métricas de consistência diária")
public class ConsistencyDto {
    @Schema(description = "Data da atividade")
    private LocalDate date;

    @Schema(description = "Total de questões respondidas no dia")
    private Integer totalAnswered;

    @Schema(description = "Total de questões respondidas corretamente no dia")
    private Integer totalCorrect;

    @Schema(description = "Tempo total gasto respondendo questões em segundos")
    private Integer totalTimeSeconds;

    @Schema(description = "Sequência de dias ativos (streak) até esta data")
    private Integer activeStreak;
}
