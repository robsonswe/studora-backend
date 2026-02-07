package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * Minimal representation for question listings.
 */
@Schema(description = "DTO simplificado para listagem de questões")
@Data
public class QuestaoSummaryDto {
    @Schema(description = "ID único da questão", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "ID do concurso ao qual a questão pertence", example = "1")
    @JsonView(Views.Summary.class)
    private Long concursoId;

    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?")
    @JsonView(Views.Summary.class)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false")
    @JsonView(Views.Summary.class)
    private Boolean anulada;

    @Schema(description = "Indica se a questão está desatualizada", example = "false")
    @JsonView(Views.Summary.class)
    private Boolean desatualizada;

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    @JsonView(Views.Summary.class)
    private String imageUrl;

    @Schema(description = "IDs dos subtemas associados à questão")
    @JsonView(Views.Summary.class)
    private List<Long> subtemaIds;

    @Schema(description = "IDs dos cargos associados à questão")
    @JsonView(Views.Summary.class)
    private List<Long> cargos;

    @Schema(description = "Alternativas da questão")
    @JsonView(Views.Summary.class)
    private List<AlternativaDto> alternativas;

    @Schema(description = "Histórico de respostas para esta questão. (Visível apenas se a questão foi respondida nos últimos 30 dias)")
    @JsonView(Views.RespostaVisivel.class)
    private List<com.studora.dto.resposta.RespostaSummaryDto> respostas;
}
