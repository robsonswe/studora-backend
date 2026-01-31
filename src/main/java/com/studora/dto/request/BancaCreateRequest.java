package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request DTO para criação de uma banca organizadora")
@Data
public class BancaCreateRequest {

    @NotBlank(message = "Nome da banca é obrigatório")
    @Schema(description = "Nome da banca organizadora", example = "CESPE", required = true)
    private String nome;
}