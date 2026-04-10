package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.subtema.TemaReferenceDto;
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

    @Schema(description = "Tema ao qual o subtema pertence")
    @JsonView(Views.Summary.class)
    private TemaReferenceDto tema;

    @Schema(description = "Disciplina à qual o subtema pertence")
    @JsonView(Views.Summary.class)
    private DisciplinaReferenceDto disciplina;
}
