package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request DTO para atualização de um cargo")
@Data
public class CargoUpdateRequest {

    @NotBlank(message = "Nome do cargo é obrigatório")
    @Schema(description = "Nome do cargo", example = "Analista de Sistemas", required = true)
    private String nome;

    @NotBlank(message = "Nível do cargo é obrigatório")
    @Schema(description = "Nível do cargo", example = "Superior", required = true)
    private String nivel;

    @NotBlank(message = "Área do cargo é obrigatória")
    @Schema(description = "Área do cargo", example = "Tecnologia da Informação", required = true)
    private String area;
}