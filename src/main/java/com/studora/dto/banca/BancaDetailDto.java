package com.studora.dto.banca;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO detalhado para visualização de uma banca")
@Data
public class BancaDetailDto {
    private Long id;
    private String nome;
    private String sigla;

    @Schema(description = "Estatísticas de questões da banca")
    private QuestaoStatsDto questaoStats;
}
