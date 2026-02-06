package com.studora.dto.cargo;

import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO detalhado para visualização de um cargo")
@Data
public class CargoDetailDto {
    private Long id;
    private String nome;
    private NivelCargo nivel;
    private String area;
}
