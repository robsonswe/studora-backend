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
    @Schema(description = "ID único da banca", example = "1")
    private Long id;

    @Schema(description = "Nome da banca organizadora", example = "CESPE")
    private String nome;
}