package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
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
    @jakarta.validation.constraints.Min(value = AppConstants.MIN_YEAR, message = "Ano deve ser no mínimo " + AppConstants.MIN_YEAR)
    @jakarta.validation.constraints.Max(value = AppConstants.MAX_YEAR, message = "Ano deve ser no máximo " + AppConstants.MAX_YEAR)
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023", required = true)
    private Integer ano;

    @NotNull(message = "Mês é obrigatório")
    @jakarta.validation.constraints.Min(value = AppConstants.MIN_MONTH, message = "Mês deve ser entre " + AppConstants.MIN_MONTH + " e " + AppConstants.MAX_MONTH)
    @jakarta.validation.constraints.Max(value = AppConstants.MAX_MONTH, message = "Mês deve ser entre " + AppConstants.MIN_MONTH + " e " + AppConstants.MAX_MONTH)
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