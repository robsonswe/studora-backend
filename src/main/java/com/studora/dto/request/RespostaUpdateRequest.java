package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO para atualização de uma resposta")
@Data
public class RespostaUpdateRequest {

    @NotNull(message = "ID da alternativa é obrigatório")
    @Schema(description = "ID da nova alternativa selecionada como resposta", example = "2", required = true)
    private Long alternativaId;

    // Constructors
    public RespostaUpdateRequest() {}

    public RespostaUpdateRequest(Long alternativaId) {
        this.alternativaId = alternativaId;
    }
}