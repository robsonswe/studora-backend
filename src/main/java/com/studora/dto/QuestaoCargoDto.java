package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para representar a associação entre questão e cargo do concurso")
@Data
public class QuestaoCargoDto {
    @Schema(description = "ID único da associação questão-cargo", example = "1")
    private Long id;

    @Schema(description = "ID da questão", example = "1")
    private Long questaoId;

    @Schema(description = "ID da associação concurso-cargo", example = "1")
    private Long concursoCargoId;
}
