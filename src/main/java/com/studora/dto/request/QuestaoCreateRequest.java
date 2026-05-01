package com.studora.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Request DTO para criação de uma questão")
@Data
public class QuestaoCreateRequest {

    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    private Boolean anulada = false;

    @Schema(description = "Indica se a questão está desatualizada", example = "false", defaultValue = "false")
    private Boolean desatualizada = false;

    @Schema(description = "Se verdadeiro, a questão é autoral e não requer concurso ou cargo.", example = "false", defaultValue = "false")
    private Boolean autoral = false;

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : null;
    }

    @Schema(description = "IDs dos subtemas associados à questão")
    private List<Long> subtemaIds; // IDs of associated subtemas

    @Schema(description = "IDs das seções da prova às quais a questão pertence (ignorado se autoral=true)")
    private List<Long> secoesIds;

    @NotNull(message = "Alternativas são obrigatórias")
    @jakarta.validation.constraints.Size(min = com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS, message = "A questão deve ter pelo menos {min} alternativas")
    @jakarta.validation.Valid
    @Schema(description = "Alternativas da questão")
    private List<AlternativaCreateRequest> alternativas; // Alternativas associated with the question

    // Constructors
    public QuestaoCreateRequest() {}


    public List<AlternativaCreateRequest> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<AlternativaCreateRequest> alternativas) {
        this.alternativas = alternativas;
    }
}
