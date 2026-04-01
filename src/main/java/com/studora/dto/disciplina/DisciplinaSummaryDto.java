package com.studora.dto.disciplina;

import com.studora.dto.DificuldadeStatDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "DTO simplificado para listagem de disciplinas")
@Data
public class DisciplinaSummaryDto {
    @Schema(description = "ID único da disciplina", example = "1")
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para todos os subtemas desta disciplina", example = "10")
    private long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas desta disciplina")
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Total de temas nesta disciplina", example = "5")
    private long totalTemas;

    @Schema(description = "Total de subtemas nesta disciplina", example = "12")
    private long totalSubtemas;

    @Schema(description = "Número de temas onde todos os subtemas possuem pelo menos uma sessão de estudo", example = "2")
    private long temasEstudados;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "8")
    private long subtemasEstudados;

    @Schema(description = "Total de questões associadas a esta disciplina (somando temas/subtemas)", example = "100")
    private long totalQuestoes;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta", example = "50")
    private long questoesRespondidas;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta correta", example = "40")
    private long questoesAcertadas;

    @Schema(description = "Tempo médio de resposta em segundos", example = "45")
    private Integer mediaTempoResposta;

    @Schema(description = "Estatísticas de respostas por nível de dificuldade (considerando apenas a resposta mais recente por questão)")
    private Map<String, DificuldadeStatDto> dificuldadeRespostas;
}
