package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Schema(description = "Request DTO para vincular uma questão a uma seção com número")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecaoQuestaoRequest {

    @NotNull(message = "ID da seção é obrigatório")
    @Schema(description = "ID da seção da prova", example = "1")
    private Long secaoId;

    @Schema(description = "Número da questão na seção", example = "1")
    private Integer numeroQuestao;

    @Schema(description = "ID da disciplina do edital", example = "1")
    private Long disciplinaEditalId;
}
