package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "DTO para representar uma questão")
@Data
public class QuestaoDto {

    @Schema(description = "ID único da questão", example = "1")
    private Long id;

    @NotNull(message = "ID do concurso é obrigatório")
    @Schema(description = "ID do concurso ao qual a questão pertence", example = "1", required = true)
    private Long concursoId;

    @NotBlank(message = "Enunciado da questão é obrigatório")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?", required = true)
    private String enunciado;

    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    private Boolean anulada = false;

    @Schema(description = "IDs dos subtemas associados à questão")
    private List<Long> subtemaIds; // IDs of associated subtemas

    @Schema(description = "IDs dos cargos do concurso associados à questão")
    private List<Long> concursoCargoIds; // IDs of associated ConcursoCargo records

    // Constructors
    public QuestaoDto() {}

    public QuestaoDto(Long concursoId, String enunciado) {
        this.concursoId = concursoId;
        this.enunciado = enunciado;
    }
}
