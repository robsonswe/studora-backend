package com.studora.dto.concurso;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.subtema.TemaReferenceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de subtema especializado para o contexto de um cargo em um concurso")
public class ConcursoCargoSubtemaDto {
    @Schema(description = "ID único do subtema", example = "1")
    private Long id;

    @Schema(description = "Nome do subtema", example = "Atos Administrativos")
    private String nome;

    @Schema(description = "Tema ao qual o subtema pertence")
    private TemaReferenceDto tema;

    @Schema(description = "Disciplina à qual o subtema pertence")
    private DisciplinaReferenceDto disciplina;

    @Schema(description = "Total de sessões de estudo realizadas para este subtema")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime ultimoEstudo;

    @Schema(description = "Estatísticas de questões do subtema (Visão Geral)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private QuestaoStatsDto questaoStats;

    @Schema(description = "Estatísticas específicas de questões para este concurso e cargo")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private com.studora.dto.StatSliceDto questoesConcursoCargo;
}
