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

    @NotNull(message = "ID do concurso é obrigatório")
    @Schema(description = "ID do concurso ao qual a questão pertence", example = "1", required = true)
    private Long concursoId;

    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", required = true)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    private Boolean anulada = false;

    @Schema(description = "Indica se a questão está desatualizada", example = "false", defaultValue = "false")
    private Boolean desatualizada = false;

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    private String imageUrl;

    @Schema(description = "IDs dos subtemas associados à questão")
    private List<Long> subtemaIds;

    @NotNull(message = "Pelo menos um cargo deve ser associado à questão")
    @Size(min = AppConstants.MIN_CARGO_ASSOCIATIONS, message = "A questão deve estar associada a pelo menos {min} cargo")
    @Schema(description = "IDs dos cargos do concurso associados à questão")
    private List<Long> concursoCargoIds;

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
