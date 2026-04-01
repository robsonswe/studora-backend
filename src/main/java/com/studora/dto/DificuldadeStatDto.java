package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estatísticas de respostas por nível de dificuldade")
public class DificuldadeStatDto {
    @Schema(description = "Total de questões respondidas neste nível de dificuldade (considerando apenas a resposta mais recente por questão)", example = "10")
    private long total;

    @Schema(description = "Total de questões acertadas neste nível de dificuldade", example = "8")
    private long corretas;
}
