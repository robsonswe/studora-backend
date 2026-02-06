package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Request DTO para associar um cargo a uma questão")
@Data
public class QuestaoCargoCreateRequest {

    @Schema(description = "ID da associação concurso-cargo", example = "1", required = true)
    private Long concursoCargoId;
}
