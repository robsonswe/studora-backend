package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.subtema.TemaReferenceDto;
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

    @Schema(description = "Tema ao qual o subtema pertence")
    @JsonView(Views.Summary.class)
    private TemaReferenceDto tema;

    @Schema(description = "Disciplina à qual o subtema pertence")
    @JsonView(Views.Summary.class)
    private DisciplinaReferenceDto disciplina;

    @Schema(description = "Quantidade de questões", example = "10")
    @JsonView(Views.Summary.class)
    private int quantidade;
}
