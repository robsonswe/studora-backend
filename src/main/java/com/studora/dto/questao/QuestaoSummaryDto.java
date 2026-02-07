package com.studora.dto.questao;

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
    private Long id;

    @Schema(description = "ID do concurso ao qual a questão pertence", example = "1")
    private Long concursoId;

    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?")
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false")
    private Boolean anulada;

    @Schema(description = "Indica se a questão está desatualizada", example = "false")
    private Boolean desatualizada;

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    private String imageUrl;

    @Schema(description = "IDs dos subtemas associados à questão")
    private List<Long> subtemaIds;

    @Schema(description = "IDs dos cargos associados à questão")
    private List<Long> cargos;
}
