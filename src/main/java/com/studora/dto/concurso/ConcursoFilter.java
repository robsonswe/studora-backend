package com.studora.dto.concurso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para filtragem dinâmica de concursos")
public class ConcursoFilter {

    @Schema(description = "Filtrar por ID da banca organizadora", example = "1")
    private Long bancaId;

    @Schema(description = "Filtrar por ID da instituição", example = "1")
    private Long instituicaoId;

    @Schema(description = "Filtrar por ID do cargo", example = "1")
    private Long cargoId;

    @Schema(description = "Filtrar por área da instituição", example = "Judiciária")
    private String instituicaoArea;

    @Schema(description = "Filtrar por área do cargo", example = "Tecnologia da Informação")
    private String cargoArea;

    @Schema(description = "Filtrar por nível do cargo", example = "SUPERIOR")
    private com.studora.entity.NivelCargo cargoNivel;
}
