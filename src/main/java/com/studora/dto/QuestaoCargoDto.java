package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para representar a associação entre questão e cargo do concurso")
@Data
public class QuestaoCargoDto {
    @Schema(description = "ID único da associação questão-cargo (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID da questão (definido pelo parâmetro da URL)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long questaoId;

    @Schema(description = "ID da associação concurso-cargo", example = "1")
    private Long concursoCargoId;
}
