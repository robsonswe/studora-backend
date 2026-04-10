package com.studora.dto.tema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "DTO detalhado para visualização de um tema")
public class TemaDetailDto {
    private Long id;
    private DisciplinaSummaryDto disciplina;
    private String nome;
    private List<SubtemaSummaryDto> subtemas;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas deste tema")
    private LocalDateTime ultimoEstudo;

    @Schema(description = "Total de subtemas neste tema", example = "3")
    private Long totalSubtemas;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "2")
    private Long subtemasEstudados;

    @Schema(description = "Estatísticas de questões do tema")
    private QuestaoStatsDto questaoStats;
}
