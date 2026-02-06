package com.studora.dto.cargo;

import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de cargos")
@Data
public class CargoSummaryDto {
    private Long id;
    private String nome;
    private NivelCargo nivel;
    private String area;
}
