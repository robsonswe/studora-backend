package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "DTO para representar uma resposta a uma questão")
@Data
public class RespostaDto {

    @Schema(description = "ID único da resposta", example = "1")
    private Long id;

    @NotNull(message = "ID da questão é obrigatório")
    @Schema(description = "ID da questão respondida", example = "1", required = true)
    private Long questaoId;

    @NotNull(message = "ID da alternativa é obrigatório")
    @Schema(description = "ID da alternativa selecionada como resposta", example = "1", required = true)
    private Long alternativaId;

    @Schema(description = "Data e hora em que a resposta foi registrada", example = "2023-06-15T10:30:00")
    private LocalDateTime respondidaEm;

    // Constructors
    public RespostaDto() {}

    public RespostaDto(Long questaoId, Long alternativaId) {
        this.questaoId = questaoId;
        this.alternativaId = alternativaId;
    }
}