package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
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

    @Schema(description = "ID da disciplina à qual o tema pertence", example = "1")
    @JsonView(Views.Summary.class)
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina à qual o tema pertence", example = "Direito Constitucional")
    @JsonView(Views.Summary.class)
    private String disciplinaNome;

    @Schema(description = "Quantidade de questões", example = "10")
    @JsonView(Views.Summary.class)
    private int quantidade;
}
