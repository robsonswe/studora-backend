package com.studora.dto.questao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para filtragem dinâmica de questões")
public class QuestaoFilter {

    @Schema(description = "Filtrar por ID da banca organizadora", example = "1")
    private Long bancaId;

    @Schema(description = "Filtrar por ID da instituição", example = "1")
    private Long instituicaoId;

    @Schema(description = "Filtrar por ID do concurso", example = "1")
    private Long concursoId;

    @Schema(description = "Filtrar por ID do cargo", example = "1")
    private Long cargoId;

    @Schema(description = "Filtrar por ID da disciplina", example = "1")
    private Long disciplinaId;

    @Schema(description = "Filtrar por ID do tema", example = "1")
    private Long temaId;

    @Schema(description = "Filtrar por ID do subtema", example = "1")
    private Long subtemaId;

    @Schema(description = "Filtrar por questões anuladas (true) ou não anuladas (false)", example = "false")
    private Boolean anulada;

    @Schema(description = "Filtrar por questões desatualizadas (true) ou atualizadas (false)", example = "false")
    private Boolean desatualizada;
}
