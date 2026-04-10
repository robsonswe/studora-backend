package com.studora.dto.disciplina;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "DTO simplificado para listagem de disciplinas")
public class DisciplinaSummaryDto {
    @Schema(description = "ID único da disciplina", example = "1")
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    private String nome;

    @Schema(description = "Data e hora do último estudo realizado")
    private LocalDateTime ultimoEstudo;

    @Schema(description = "Total de temas associados", example = "5")
    private Long totalTemas;

    @Schema(description = "Total de subtemas associados", example = "15")
    private Long totalSubtemas;

    @Schema(description = "Total de subtemas estudados", example = "10")
    private Long subtemasEstudados;

    @Schema(description = "Total de temas estudados", example = "3")
    private Long temasEstudados;

    @Schema(description = "Estatísticas de questões da disciplina")
    private QuestaoStatsDto questaoStats;
}
