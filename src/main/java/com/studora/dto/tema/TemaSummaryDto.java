package com.studora.dto.tema;

import com.studora.dto.DificuldadeStatDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "DTO simplificado para listagem de temas")
@Data
public class TemaSummaryDto {
    @Schema(description = "ID único do tema", example = "1")
    private Long id;

    @Schema(description = "ID da disciplina à qual o tema pertence", example = "1")
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina à qual o tema pertence", example = "Direito Constitucional")
    private String disciplinaNome;

    @Schema(description = "Nome do tema", example = "Direitos Fundamentais")
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para todos os subtemas deste tema", example = "5")
    private long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas deste tema")
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Data e hora da última questão respondida entre todos os subtemas deste tema")
    private java.time.LocalDateTime ultimaQuestao;

    @Schema(description = "Total de subtemas neste tema", example = "3")
    private long totalSubtemas;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "2")
    private long subtemasEstudados;

    @Schema(description = "Total de questões associadas a este tema (somando subtemas)", example = "30")
    private long totalQuestoes;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta", example = "20")
    private long questoesRespondidas;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta correta", example = "15")
    private long questoesAcertadas;

    @Schema(description = "Tempo médio de resposta em segundos", example = "45")
    private Integer mediaTempoResposta;

    @Schema(description = "Estatísticas de respostas por nível de dificuldade (considerando apenas a resposta mais recente por questão)")
    private Map<String, DificuldadeStatDto> dificuldadeRespostas;
}
