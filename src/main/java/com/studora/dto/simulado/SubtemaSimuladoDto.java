package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO de subtema dentro de um simulado com quantidade")
@Data
public class SubtemaSimuladoDto {
    @Schema(description = "ID único do subtema", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome do subtema", example = "Habeas Corpus")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "ID do tema ao qual o subtema pertence", example = "1")
    @JsonView(Views.Summary.class)
    private Long temaId;

    @Schema(description = "Nome do tema ao qual o subtema pertence", example = "Remédios Constitucionais")
    @JsonView(Views.Summary.class)
    private String temaNome;

    @Schema(description = "ID da disciplina", example = "1")
    @JsonView(Views.Summary.class)
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    @JsonView(Views.Summary.class)
    private String disciplinaNome;

    @Schema(description = "Quantidade de questões", example = "10")
    @JsonView(Views.Summary.class)
    private int quantidade;
}
