package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import com.studora.common.constants.AppConstants;

@Schema(description = "Request DTO para atualização de uma questão")
@Data
public class QuestaoUpdateRequest {

    @Schema(description = "ID do concurso ao qual a questão pertence (ignorado se autoral=true)", example = "1")
    private Long concursoId;

    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", required = true)
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

    @Schema(description = "IDs dos cargos associados à questão (ignorado se autoral=true)")
    private List<Long> cargos;

    @Schema(description = "Alternativas da questão")
    @Size(min = AppConstants.MIN_ALTERNATIVAS, message = "A questão deve ter pelo menos {min} alternativas")
    @jakarta.validation.Valid
    private List<AlternativaUpdateRequest> alternativas;

    public QuestaoUpdateRequest() {}

    public QuestaoUpdateRequest(Long concursoId, String enunciado) {
        this.concursoId = concursoId;
        this.enunciado = enunciado;
    }
}
