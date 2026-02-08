package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO com contexto do concurso para exibição em questões")
@Data
public class ConcursoQuestaoDto {
    @Schema(description = "ID do concurso", example = "10")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Ano do concurso", example = "2024")
    @JsonView(Views.Summary.class)
    private Integer ano;

    @Schema(description = "ID da banca", example = "2")
    @JsonView(Views.Summary.class)
    private Long bancaId;

    @Schema(description = "Nome da banca", example = "Cebraspe")
    @JsonView(Views.Summary.class)
    private String bancaNome;

    @Schema(description = "ID da instituição", example = "3")
    @JsonView(Views.Summary.class)
    private Long instituicaoId;

    @Schema(description = "Nome da instituição", example = "Policia Federal")
    @JsonView(Views.Summary.class)
    private String instituicaoNome;

    @Schema(description = "Área da instituição", example = "Segurança")
    @JsonView(Views.Summary.class)
    private String instituicaoArea;
}
