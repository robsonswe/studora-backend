package com.studora.dto.disciplina;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.tema.TemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
@Schema(description = "DTO detalhado de Disciplina")
public class DisciplinaDetailDto {
    private Long id;
    private String nome;
    private List<TemaSummaryDto> temas;

    @Schema(description = "Total de sessões de estudo realizadas para todos os subtemas desta disciplina", example = "10")
    private Long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas desta disciplina")
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Data e hora da última questão respondida entre todos os subtemas desta disciplina")
    private java.time.LocalDateTime ultimaQuestao;

    @Schema(description = "Total de temas nesta disciplina", example = "5")
    private Long totalTemas;

    @Schema(description = "Total de subtemas nesta disciplina", example = "12")
    private Long totalSubtemas;

    @Schema(description = "Número de temas onde todos os subtemas possuem pelo menos uma sessão de estudo", example = "2")
    private Long temasEstudados;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "8")
    private Long subtemasEstudados;

    @Schema(description = "Total de questões associadas a esta disciplina (somando temas/subtemas)", example = "100")
    private Long totalQuestoes;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta", example = "50")
    private Long questoesRespondidas;

    @Schema(description = "Total de questões que possuem pelo menos uma resposta correta", example = "40")
    private Long questoesAcertadas;

    @Schema(description = "Tempo médio de resposta em segundos", example = "45")
    private Integer mediaTempoResposta;

    @Schema(description = "Estatísticas de respostas por nível de dificuldade (considerando apenas a resposta mais recente por questão)")
    private Map<String, DificuldadeStatDto> dificuldadeRespostas;
}