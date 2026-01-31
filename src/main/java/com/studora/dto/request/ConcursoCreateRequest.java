package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request DTO para criação de um concurso")
@Data
public class ConcursoCreateRequest {

    @NotNull(message = "ID da instituição é obrigatório")
    @Schema(description = "ID da instituição organizadora do concurso", example = "1", required = true)
    private Long instituicaoId;

    @NotNull(message = "ID da banca é obrigatório")
    @Schema(description = "ID da banca organizadora do concurso", example = "1", required = true)
    private Long bancaId;

    @NotNull(message = "Ano é obrigatório")
    @jakarta.validation.constraints.Min(value = 1900, message = "Ano deve ser no mínimo 1900")
    @jakarta.validation.constraints.Max(value = 2100, message = "Ano deve ser no máximo 2100")
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023", required = true)
    private Integer ano;

    @NotNull(message = "Mês é obrigatório")
    @jakarta.validation.constraints.Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @jakarta.validation.constraints.Max(value = 12, message = "Mês deve ser entre 1 e 12")
    @Schema(description = "Mês em que o concurso foi realizado", example = "6", required = true)
    private Integer mes;

    @jakarta.validation.constraints.Size(min = 1, message = "Edital não pode ser uma string vazia")
    @Schema(description = "Identificação do edital do concurso", example = "Edital 01/2023")
    private String edital;

    // Constructors
    public ConcursoCreateRequest() {}

    public ConcursoCreateRequest(Long instituicaoId, Long bancaId, Integer ano, Integer mes) {
        this.instituicaoId = instituicaoId;
        this.bancaId = bancaId;
        this.ano = ano;
        this.mes = mes;
    }
}