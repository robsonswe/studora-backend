package com.studora.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "DTO para representar uma questão")
@Data
public class QuestaoDto {

    @Schema(description = "ID único da questão (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonView(Views.SimuladoIniciado.class)
    private Long id;

    @NotNull(message = "ID do concurso é obrigatório")
    @Schema(description = "ID do concurso ao qual a questão pertence", example = "1", required = true)
    @JsonView(Views.SimuladoIniciado.class)
    private Long concursoId;

    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", required = true)
    @JsonView(Views.SimuladoIniciado.class)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    @JsonView(Views.SimuladoIniciado.class)
    private Boolean anulada = false;

    @Schema(description = "Indica se a questão está desatualizada", example = "false", defaultValue = "false")
    @JsonView(Views.SimuladoIniciado.class)
    private Boolean desatualizada = false;

    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    @JsonView(Views.SimuladoIniciado.class)
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : null;
    }

    @Schema(description = "IDs dos subtemas associados à questão")
    @JsonView(Views.SimuladoIniciado.class)
    private List<Long> subtemaIds; // IDs of associated subtemas

    @Schema(description = "IDs dos cargos do concurso associados à questão")
    @JsonView(Views.SimuladoIniciado.class)
    private List<Long> concursoCargoIds; // IDs of associated ConcursoCargo records

    @Schema(description = "Alternativas da questão")
    @JsonView(Views.SimuladoIniciado.class)
    private List<AlternativaDto> alternativas; // Alternativas associated with the question

    @Schema(description = "Resposta do usuário para esta questão dentro do contexto do simulado")
    @JsonView(Views.SimuladoFinalizado.class)
    private RespostaDto resposta;

    // Constructors
    public QuestaoDto() {}

    public QuestaoDto(Long concursoId, String enunciado) {
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
