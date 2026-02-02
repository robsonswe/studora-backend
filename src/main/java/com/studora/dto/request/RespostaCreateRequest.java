package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO para criação de uma resposta")
@Data
public class RespostaCreateRequest {

    @NotNull(message = "ID da questão é obrigatório")
    @Schema(description = "ID da questão respondida", example = "1", required = true)
    private Long questaoId;

    @NotNull(message = "ID da alternativa é obrigatório")
    @Schema(description = "ID da alternativa selecionada como resposta", example = "1", required = true)
    private Long alternativaId;

    @Schema(description = "Raciocínio ou comentário do usuário para esta tentativa", example = "Achei que era a B por causa de...")
    private String justificativa;

    @Schema(description = "ID do grau de dificuldade percebido (1=Fácil, 2=Média, 3=Difícil, 4=Chute)", example = "2")
    private Integer dificuldadeId;

    @Schema(description = "Duração da tentativa em segundos", example = "45")
    private Integer tempoRespostaSegundos;

    // Constructors
    public RespostaCreateRequest() {}

    public RespostaCreateRequest(Long questaoId, Long alternativaId) {
        this.questaoId = questaoId;
        this.alternativaId = alternativaId;
    }
}