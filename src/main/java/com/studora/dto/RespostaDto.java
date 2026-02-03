package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "DTO para representar uma resposta a uma questão")
@Data
public class RespostaDto {

    @Schema(description = "ID único da resposta (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "ID da questão é obrigatório")
    @Schema(description = "ID da questão respondida", example = "1", required = true)
    private Long questaoId;

    @NotNull(message = "ID da alternativa é obrigatório")
    @Schema(description = "ID da alternativa selecionada como resposta", example = "1", required = true)
    private Long alternativaId;

    @Schema(description = "Indica se a alternativa selecionada é a correta", example = "true")
    private Boolean correta;

    @Schema(description = "Raciocínio ou comentário do usuário para esta tentativa", example = "Achei que era a B por causa de...")
    private String justificativa;

    @Schema(description = "ID do grau de dificuldade percebido (1=Fácil, 2=Média, 3=Difícil, 4=Chute)", example = "2")
    private Integer dificuldadeId;

    @Schema(description = "ID do simulado ao qual esta resposta pertence", example = "1")
    private Long simuladoId;

    @Schema(description = "Duração da tentativa em segundos", example = "45")
    private Integer tempoRespostaSegundos;

    @Schema(description = "Data e hora em que a resposta foi registrada (gerada automaticamente)", example = "2023-06-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private java.time.LocalDateTime createdAt;

    // Constructors
    public RespostaDto() {}

    public RespostaDto(Long questaoId, Long alternativaId) {
        this.questaoId = questaoId;
        this.alternativaId = alternativaId;
    }
}