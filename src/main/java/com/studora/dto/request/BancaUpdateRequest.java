package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request DTO para atualização de uma banca organizadora")
@Data
public class BancaUpdateRequest {

    @NotBlank(message = "Nome da banca é obrigatório")
    @Schema(description = "Nome da banca organizadora", example = "CESPE", required = true)
    private String nome;
}