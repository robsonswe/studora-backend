package com.studora.dto.cargo;

import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de cargos")
@Data
public class CargoSummaryDto {
    @Schema(description = "ID único do cargo", example = "1")
    private Long id;

    @Schema(description = "Nome do cargo", example = "Analista Judiciário")
    private String nome;

    @Schema(description = "Nível de escolaridade do cargo", example = "SUPERIOR")
    private NivelCargo nivel;

    @Schema(description = "Área de atuação do cargo", example = "Judiciária")
    private String area;
}
