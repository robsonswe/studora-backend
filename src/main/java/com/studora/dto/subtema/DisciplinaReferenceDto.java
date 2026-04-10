package com.studora.dto.subtema;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "Referência simplificada para disciplina")
public class DisciplinaReferenceDto {
    @Schema(description = "ID da disciplina", example = "1")
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    private String nome;
}
