package com.studora.dto.subtema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "DTO simplificado para listagem de subtemas")
public class SubtemaSummaryDto {
    @Schema(description = "ID único do subtema", example = "1")
    private Long id;

    @Schema(description = "Tema ao qual o subtema pertence")
    private TemaReferenceDto tema;

    @Schema(description = "Disciplina à qual o subtema pertence")
    private DisciplinaReferenceDto disciplina;

    @Schema(description = "Nome do subtema", example = "Atos Administrativos")
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para este subtema")
    private Long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado")
    private LocalDateTime ultimoEstudo;

    @Schema(description = "Estatísticas de questões do subtema")
    private QuestaoStatsDto questaoStats;
}
