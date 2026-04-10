package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO de tema dentro de um simulado com quantidade")
@Data
public class TemaSimuladoDto {
    @Schema(description = "ID único do tema", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome do tema", example = "Direitos Fundamentais")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "Disciplina à qual o tema pertence")
    @JsonView(Views.Summary.class)
    private DisciplinaReferenceDto disciplina;

    @Schema(description = "Quantidade de questões", example = "10")
    @JsonView(Views.Summary.class)
    private int quantidade;
}
