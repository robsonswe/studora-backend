package com.studora.dto;

import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para representar um cargo")
@Data
public class CargoDto {
    @Schema(description = "ID único do cargo (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome do cargo", example = "Analista de Sistemas")
    private String nome;

    @Schema(description = "Nível do cargo", example = "SUPERIOR")
    private NivelCargo nivel;

    @Schema(description = "Área do cargo", example = "Tecnologia da Informação")
    private String area;
}
