package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO simplificado de uma seção de prova no contexto de uma questão")
public class SecaoQuestaoDto {
    @Schema(description = "ID da seção", example = "100")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome da seção", example = "Conhecimentos Específicos")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "Nome da prova à qual a seção pertence", example = "Prova Objetiva")
    @JsonView(Views.Summary.class)
    private String provaNome;

    @Schema(description = "ID da prova à qual a seção pertence", example = "10")
    @JsonView(Views.Summary.class)
    private Long provaId;

    @Schema(description = "Número da questão na seção", example = "1")
    @JsonView(Views.Summary.class)
    private Integer numeroQuestao;
    @Schema(description = "ID da disciplina do edital", example = "1")
    @JsonView(Views.Summary.class)
    private Long disciplinaEditalId;

    @Schema(description = "Nome da disciplina do edital", example = "Língua Portuguesa")
    @JsonView(Views.Summary.class)
    private String disciplinaEditalNome;
}