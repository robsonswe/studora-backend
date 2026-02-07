package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.resposta.RespostaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * Detailed representation for single question view.
 */
@Schema(description = "DTO detalhado para visualização de uma questão")
@Data
public class QuestaoDetailDto {
    @Schema(description = "ID único da questão", example = "1")
    @JsonView(Views.RespostaOculta.class)
    private Long id;

    @Schema(description = "ID do concurso ao qual a questão pertence", example = "1")
    @JsonView(Views.RespostaOculta.class)
    private Long concursoId;

    @Schema(description = "Dados do concurso ao qual a questão pertence")
    @JsonView(Views.RespostaOculta.class)
    private ConcursoSummaryDto concurso;

    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?")
    @JsonView(Views.RespostaOculta.class)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false")
    @JsonView(Views.QuestaoOculta.class)
    private Boolean anulada;

    @Schema(description = "Indica se a questão está desatualizada", example = "false")
    @JsonView(Views.RespostaOculta.class)
    private Boolean desatualizada;

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    @JsonView(Views.RespostaOculta.class)
    private String imageUrl;

    @Schema(description = "IDs dos subtemas associados à questão", example = "[1, 2]")
    @JsonView(Views.RespostaOculta.class)
    private List<Long> subtemaIds;

    @Schema(description = "Dados dos subtemas associados à questão")
    @JsonView(Views.RespostaOculta.class)
    private List<SubtemaSummaryDto> subtemas;

    @Schema(description = "IDs dos cargos associados à questão", example = "[1]")
    @JsonView(Views.RespostaOculta.class)
    private List<Long> cargos;

    @Schema(description = "Alternativas da questão")
    @JsonView(Views.RespostaOculta.class)
    private List<AlternativaDto> alternativas;

    @Schema(description = "Resposta do usuário para esta questão dentro do contexto do simulado")
    @JsonView(Views.RespostaVisivel.class)
    private RespostaSummaryDto resposta;

    @Schema(description = "Histórico de respostas para esta questão. (Visível apenas se a questão foi respondida nos últimos 30 dias)")
    @JsonView(Views.RespostaVisivel.class)
    private List<RespostaSummaryDto> respostas;
}