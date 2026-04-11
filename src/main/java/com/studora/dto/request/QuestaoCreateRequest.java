package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Request DTO para criação de uma questão")
@Data
public class QuestaoCreateRequest {

    @Schema(description = "ID do concurso ao qual a questão pertence (ignorada se autoral=true)", example = "1")
    private Long concursoId;

    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", required = true)
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

    @jakarta.validation.constraints.Size(min = com.studora.common.constants.AppConstants.MIN_CARGO_ASSOCIATIONS, message = "A questão deve estar associada a pelo menos {min} cargo")
    @Schema(description = "IDs dos cargos associados à questão (ignorado se autoral=true)")
    private List<Long> cargos; // IDs of associated Cargo records

    @NotNull(message = "Alternativas são obrigatórias")
    @jakarta.validation.constraints.Size(min = com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS, message = "A questão deve ter pelo menos {min} alternativas")
    @jakarta.validation.Valid
    @Schema(description = "Alternativas da questão")
    private List<AlternativaCreateRequest> alternativas; // Alternativas associated with the question

    // Constructors
    public QuestaoCreateRequest() {}

    public QuestaoCreateRequest(Long concursoId, String enunciado) {
        this.concursoId = concursoId;
        this.enunciado = enunciado;
    }

    public List<AlternativaCreateRequest> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<AlternativaCreateRequest> alternativas) {
        this.alternativas = alternativas;
    }
}
