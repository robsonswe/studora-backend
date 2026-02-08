package com.studora.dto.tema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de temas")
@Data
public class TemaSummaryDto {
    @Schema(description = "ID único do tema", example = "1")
    private Long id;

    @Schema(description = "ID da disciplina à qual o tema pertence", example = "1")
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina à qual o tema pertence", example = "Direito Constitucional")
    private String disciplinaNome;

    @Schema(description = "Nome do tema", example = "Direitos Fundamentais")
    private String nome;
}
