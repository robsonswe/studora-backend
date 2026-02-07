package com.studora.dto.banca;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de bancas")
@Data
public class BancaSummaryDto {
    @Schema(description = "ID único da banca", example = "1")
    private Long id;

    @Schema(description = "Nome da banca organizadora", example = "Cebraspe (CESPE)")
    private String nome;
}
