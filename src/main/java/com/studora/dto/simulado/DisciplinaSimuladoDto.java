package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO de disciplina dentro de um simulado com quantidade")
@Data
public class DisciplinaSimuladoDto {
    @Schema(description = "ID único da disciplina", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "Quantidade de questões", example = "10")
    @JsonView(Views.Summary.class)
    private int quantidade;
}
