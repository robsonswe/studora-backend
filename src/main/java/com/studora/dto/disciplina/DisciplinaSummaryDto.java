package com.studora.dto.disciplina;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Minimal representation for disciplina listings.
 */
@Schema(description = "DTO simplificado para listagem de disciplinas")
@Data
public class DisciplinaSummaryDto {
    @Schema(description = "ID único da disciplina", example = "1")
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    private String nome;
}
