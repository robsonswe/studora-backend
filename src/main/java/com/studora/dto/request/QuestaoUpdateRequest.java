package com.studora.dto.request;

import com.studora.dto.AlternativaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : null;
    }

    @Schema(description = "IDs dos subtemas associados à questão")
    private List<Long> subtemaIds; // IDs of associated subtemas

    @Schema(description = "IDs dos cargos do concurso associados à questão")
    private List<Long> concursoCargoIds; // IDs of associated ConcursoCargo records

    @Schema(description = "Alternativas da questão")
    private List<AlternativaDto> alternativas; // Alternativas associated with the question

    // Constructors
    public QuestaoUpdateRequest() {}

    public QuestaoUpdateRequest(Long concursoId, String enunciado) {
        this.concursoId = concursoId;
        this.enunciado = enunciado;
    }

    public List<AlternativaDto> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<AlternativaDto> alternativas) {
        this.alternativas = alternativas;
    }
}