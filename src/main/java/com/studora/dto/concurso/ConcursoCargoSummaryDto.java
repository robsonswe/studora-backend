package com.studora.dto.concurso;

import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO que representa a associação de um cargo a um concurso com status de inscrição")
@Data
public class ConcursoCargoSummaryDto {
    @Schema(description = "ID da associação concurso-cargo", example = "1")
    private Long id;

    @Schema(description = "ID do cargo", example = "1")
    private Long cargoId;

    @Schema(description = "Nome do cargo", example = "Agente")
    private String cargoNome;

    @Schema(description = "Nível de escolaridade do cargo")
    private NivelCargo nivel;

    @Schema(description = "Área de atuação do cargo", example = "Policial")
    private String area;

    @Schema(description = "Indica se o usuário está inscrito para este cargo neste concurso")
    private boolean inscrito;
}
