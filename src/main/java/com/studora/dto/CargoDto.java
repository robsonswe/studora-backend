package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para representar um cargo")
@Data
public class CargoDto {
    @Schema(description = "ID único do cargo", example = "1")
    private Long id;

    @Schema(description = "Nome do cargo", example = "Analista de Sistemas")
    private String nome;

    @Schema(description = "Nível do cargo", example = "Superior")
    private String nivel;

    @Schema(description = "Área do cargo", example = "Tecnologia da Informação")
    private String area;
}
