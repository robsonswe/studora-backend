package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO com hierarquia do subtema para exibição em questões")
@Data
public class SubtemaQuestaoDto {
    @Schema(description = "ID do subtema", example = "5")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome do subtema", example = "Habeas Corpus")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "ID do tema", example = "20")
    @JsonView(Views.Summary.class)
    private Long temaId;

    @Schema(description = "Nome do tema", example = "Remédios Constitucionais")
    @JsonView(Views.Summary.class)
    private String temaNome;

    @Schema(description = "ID da disciplina", example = "100")
    @JsonView(Views.Summary.class)
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    @JsonView(Views.Summary.class)
    private String disciplinaNome;
}
