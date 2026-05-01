package com.studora.dto.request;

import java.util.List;

import com.studora.common.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request DTO para atualização de uma questão")
@Data
public class QuestaoUpdateRequest {


    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    private Boolean anulada = false;

    @Schema(description = "Indica se a questão está desatualizada", example = "false", defaultValue = "false")
    private Boolean desatualizada = false;

    @Schema(description = "Tipo da questão. Não pode ser alterado após a criação.", example = "false")
    private Boolean autoral; // read-only intent; used only for validation guard

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    private String imageUrl;

    @Schema(description = "IDs dos subtemas associados à questão")
    @com.fasterxml.jackson.annotation.JsonProperty("subtemas")
    private List<Long> subtemaIds;

    @Schema(description = "IDs das seções da prova às quais a questão pertence (ignorado se autoral=true)")
    private List<Long> secoesIds;

    @Schema(description = "Alternativas da questão")
    @Size(min = AppConstants.MIN_ALTERNATIVAS, message = "A questão deve ter pelo menos {min} alternativas")
    @jakarta.validation.Valid
    private List<AlternativaUpdateRequest> alternativas;

    public QuestaoUpdateRequest() {}

    public QuestaoUpdateRequest(String enunciado) {
        this.enunciado = enunciado;
    }
}
