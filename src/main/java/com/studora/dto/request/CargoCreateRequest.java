package com.studora.dto.request;

import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO para criação de um cargo")
@Data
public class CargoCreateRequest {

    @NotBlank(message = "Nome do cargo é obrigatório")
    @Schema(description = "Nome do cargo", example = "Analista de Sistemas", required = true)
    private String nome;

    @NotNull(message = "Nível do cargo é obrigatório")
    @Schema(description = "Nível do cargo", example = "SUPERIOR", required = true, implementation = NivelCargo.class)
    private NivelCargo nivel;

    @NotBlank(message = "Área do cargo é obrigatória")
    @Schema(description = "Área do cargo", example = "Tecnologia da Informação", required = true)
    private String area;
}
