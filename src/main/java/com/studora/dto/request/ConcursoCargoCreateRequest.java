package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO para associar um cargo a um concurso")
@Data
public class ConcursoCargoCreateRequest {

    @NotNull(message = "ID do cargo é obrigatório")
    @Schema(description = "ID do cargo a ser associado ao concurso", example = "1", required = true)
    private Long cargoId;
}