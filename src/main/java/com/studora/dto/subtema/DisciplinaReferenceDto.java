package com.studora.dto.subtema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "Referência simplificada para disciplina")
public class DisciplinaReferenceDto {
    @Schema(description = "ID da disciplina", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    @JsonView(Views.Summary.class)
    private String nome;
}
