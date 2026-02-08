package com.studora.dto.cargo;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de cargos")
@Data
public class CargoSummaryDto {
    @Schema(description = "ID único do cargo", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome do cargo", example = "Analista Judiciário")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "Nível de escolaridade do cargo", example = "SUPERIOR")
    @JsonView(Views.Summary.class)
    private NivelCargo nivel;

    @Schema(description = "Área de atuação do cargo", example = "Judiciária")
    @JsonView(Views.Summary.class)
    private String area;
}
