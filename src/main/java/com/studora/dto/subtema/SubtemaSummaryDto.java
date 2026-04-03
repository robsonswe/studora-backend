package com.studora.dto.subtema;

import com.studora.dto.DificuldadeStatDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "DTO simplificado para listagem de subtemas")
@Data
public class SubtemaSummaryDto {
    @Schema(description = "ID único do subtema", example = "1")
    private Long id;

    @Schema(description = "ID do tema ao qual o subtema pertence", example = "1")
    private Long temaId;

    @Schema(description = "Nome do tema ao qual o subtema pertence", example = "Atos Administrativos")
    private String temaNome;

    @Schema(description = "ID da disciplina à qual o subtema pertence", example = "1")
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina à qual o subtema pertence", example = "Direito Administrativo")
    private String disciplinaNome;

    @Schema(description = "Nome do subtema", example = "Espécies de Atos")
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para este subtema", example = "2")
    private long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado")
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
