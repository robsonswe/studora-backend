package com.studora.dto.instituicao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO detalhado para visualização de uma instituição")
@Data
public class InstituicaoDetailDto {
    private Long id;
    private String nome;
    private String area;
    private String sigla;

    @Schema(description = "Estatísticas de questões da instituição")
    private QuestaoStatsDto questaoStats;
}
