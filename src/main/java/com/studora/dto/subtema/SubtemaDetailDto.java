package com.studora.dto.subtema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "DTO detalhado para visualização de um subtema")
public class SubtemaDetailDto {
    private Long id;
    private TemaReferenceDto tema;
    private DisciplinaReferenceDto disciplina;
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para este subtema")
    private Long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado")
    private LocalDateTime ultimoEstudo;

    @Schema(description = "Estatísticas de questões do subtema")
    private QuestaoStatsDto questaoStats;
}
