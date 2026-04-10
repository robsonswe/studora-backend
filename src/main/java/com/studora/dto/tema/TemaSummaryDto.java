package com.studora.dto.tema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "DTO simplificado para listagem de temas")
public class TemaSummaryDto {
    @Schema(description = "ID único do tema", example = "1")
    private Long id;

    @Schema(description = "Disciplina à qual o tema pertence")
    private DisciplinaReferenceDto disciplina;

    @Schema(description = "Nome do tema", example = "Direitos Fundamentais")
    private String nome;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas deste tema")
    private LocalDateTime ultimoEstudo;

    @Schema(description = "Total de subtemas neste tema", example = "3")
    private Long totalSubtemas;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "2")
    private Long subtemasEstudados;

    @Schema(description = "Estatísticas de questões do tema")
    private QuestaoStatsDto questaoStats;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Lista de subtemas associados a este tema (populated in completo endpoint)")
    private List<SubtemaSummaryDto> subtemas;
}
