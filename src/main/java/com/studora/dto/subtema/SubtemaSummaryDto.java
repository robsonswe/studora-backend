package com.studora.dto.subtema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de subtemas")
@Data
public class SubtemaSummaryDto {
    @Schema(description = "ID único do subtema", example = "1")
    private Long id;

    @Schema(description = "ID do tema ao qual o subtema pertence", example = "1")
    private Long temaId;

    @Schema(description = "Nome do subtema", example = "Atos Administrativos")
    private String nome;
}
