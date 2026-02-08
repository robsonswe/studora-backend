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

    @Schema(description = "Nome do tema ao qual o subtema pertence", example = "Atos Administrativos")
    private String temaNome;

    @Schema(description = "ID da disciplina à qual o subtema pertence", example = "1")
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina à qual o subtema pertence", example = "Direito Administrativo")
    private String disciplinaNome;

    @Schema(description = "Nome do subtema", example = "Espécies de Atos")
    private String nome;
}
