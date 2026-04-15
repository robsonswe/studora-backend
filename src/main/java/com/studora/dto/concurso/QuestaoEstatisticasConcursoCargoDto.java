package com.studora.dto.concurso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.DificuldadeStatDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estatísticas de questões deste subtema neste concurso+cargo")
public class QuestaoEstatisticasConcursoCargoDto {
    @Schema(description = "Total de questões deste subtema neste concurso+cargo")
    private Long totalQuestoes;

    @Schema(description = "Questões já respondidas pelo usuário neste contexto")
    private Long respondidas;

    @Schema(description = "Questões acertadas pelo usuário neste contexto")
    private Long acertadas;

    @Schema(description = "Média de tempo de resposta em segundos")
    private Integer mediaTempoResposta;

    @Schema(description = "Data da última questão respondida")
    private LocalDateTime ultimaQuestao;

    @Schema(description = "Breakdown por dificuldade")
    private Map<String, DificuldadeStatDto> dificuldade;
}
