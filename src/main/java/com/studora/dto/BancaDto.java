package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para representar uma banca organizadora")
public class BancaDto {
    @Schema(description = "ID único da banca (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome da banca organizadora", example = "CESPE")
    private String nome;
}