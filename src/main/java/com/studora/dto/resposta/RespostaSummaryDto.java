package com.studora.dto.resposta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para resumo de resposta")
public class RespostaSummaryDto {
    private Long id;
    private Long questaoId;
    private Long alternativaId;
    private Boolean correta;
    private String justificativa;
    private Integer tempoRespostaSegundos;
    private Long simuladoId;
    private LocalDateTime createdAt;
}
