package com.studora.dto.cargo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studora.dto.QuestaoStatsDto;
import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO detalhado para visualização de um cargo")
@Data
public class CargoDetailDto {
    private Long id;
    private String nome;
    private NivelCargo nivel;
    private String area;

    @Schema(description = "Estatísticas de questões do cargo")
    private QuestaoStatsDto questaoStats;
}
