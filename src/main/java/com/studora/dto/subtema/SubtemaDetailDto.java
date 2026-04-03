package com.studora.dto.subtema;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.tema.TemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "DTO detalhado para visualização de um subtema")
@Data
public class SubtemaDetailDto {
    private Long id;
    private TemaSummaryDto tema;
    private String nome;
    private long totalEstudos;
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Data e hora da última questão respondida")
    private java.time.LocalDateTime ultimaQuestao;

    @Schema(description = "Total de questões associadas a este subtema", example = "15")
    private long totalQuestoes;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta", example = "10")
    private long questoesRespondidas;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta correta", example = "8")
    private long questoesAcertadas;

    @Schema(description = "Tempo médio de resposta em segundos", example = "45")
    private Integer mediaTempoResposta;

    @Schema(description = "Estatísticas de respostas por nível de dificuldade (considerando apenas a resposta mais recente por questão)")
    private Map<String, DificuldadeStatDto> dificuldadeRespostas;
}
