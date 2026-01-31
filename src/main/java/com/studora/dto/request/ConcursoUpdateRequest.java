package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request DTO para atualização de um concurso")
@Data
public class ConcursoUpdateRequest {

    @NotNull(message = "ID da instituição é obrigatório")
    @Schema(description = "ID da instituição organizadora do concurso", example = "1", required = true)
    private Long instituicaoId;

    @NotNull(message = "ID da banca é obrigatório")
    @Schema(description = "ID da banca organizadora do concurso", example = "1", required = true)
    private Long bancaId;

    @NotNull(message = "Ano é obrigatório")
    @Positive(message = "Ano deve ser um número positivo")
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023", required = true)
    private Integer ano;

    // Constructors
    public ConcursoUpdateRequest() {}

    public ConcursoUpdateRequest(Long instituicaoId, Long bancaId, Integer ano) {
        this.instituicaoId = instituicaoId;
        this.bancaId = bancaId;
        this.ano = ano;
    }
}