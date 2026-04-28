package com.studora.dto.instituicao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO simplificado para listagem de instituições")
@Data
public class InstituicaoSummaryDto {
    @Schema(description = "ID único da instituição", example = "1")
    private Long id;

    @Schema(description = "Nome da instituição", example = "Tribunal de Justiça de São Paulo")
    private String nome;

    @Schema(description = "Área de atuação da instituição", example = "Judiciária")
    private String area;

    @Schema(description = "Sigla da instituição", example = "TJSP")
    private String sigla;

    @Schema(description = "Estatísticas de questões da instituição")
    private QuestaoStatsDto questaoStats;
}
