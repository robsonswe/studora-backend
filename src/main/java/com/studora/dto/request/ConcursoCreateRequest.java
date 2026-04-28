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
    @Schema(description = "ID da instituição organizadora do concurso", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long instituicaoId;

    @NotNull(message = "ID da banca é obrigatório")
    @Schema(description = "ID da banca organizadora do concurso", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bancaId;

    @NotNull(message = "Ano é obrigatório")
    @jakarta.validation.constraints.Min(value = AppConstants.MIN_YEAR, message = "Ano deve ser no mínimo " + AppConstants.MIN_YEAR)
    @jakarta.validation.constraints.Max(value = AppConstants.MAX_YEAR, message = "Ano deve ser no máximo " + AppConstants.MAX_YEAR)
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ano;

    @NotNull(message = "Mês é obrigatório")
    @jakarta.validation.constraints.Min(value = AppConstants.MIN_MONTH, message = "Mês deve ser entre " + AppConstants.MIN_MONTH + " e " + AppConstants.MAX_MONTH)
    @jakarta.validation.constraints.Max(value = AppConstants.MAX_MONTH, message = "Mês deve ser entre " + AppConstants.MIN_MONTH + " e " + AppConstants.MAX_MONTH)
    @Schema(description = "Mês em que o concurso foi realizado", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer mes;

    @jakarta.validation.constraints.Pattern(regexp = "^$|^(https?://.*|#[A-Za-z0-9]+)$", message = "Edital deve ser uma URL válida")
    @Schema(description = "URL do edital do concurso", example = "https://exemplo.com/edital.pdf")
    private String edital;

    @Schema(description = "Data e hora da prova do concurso", example = "2024-06-15T08:00:00")
    private java.time.LocalDateTime dataProva;

    @Schema(description = "Indica se o concurso já foi finalizado", example = "true")
    private boolean finalizado;

    @NotNull(message = "A lista de cargos é obrigatória")
    @jakarta.validation.constraints.NotEmpty(message = "O concurso deve ter pelo menos um cargo")
    @Schema(description = "Lista de IDs dos cargos associados ao concurso", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    private java.util.List<Long> cargos;

    @Schema(
        description = "Mapa de subtemas para cargos. Chave: subtemaId, Valor: lista de cargoIds associados a este subtema neste concurso. Cada subtema deve ter pelo menos 1 cargo.",
        example = "{\"12\": [1, 2], \"5\": [1]}"
    )
    private java.util.Map<Long, java.util.List<Long>> topicos;

    // Constructors
    public ConcursoCreateRequest() {}

    public ConcursoCreateRequest(Long instituicaoId, Long bancaId, Integer ano, Integer mes, java.util.List<Long> cargos) {
        this.instituicaoId = instituicaoId;
        this.bancaId = bancaId;
        this.ano = ano;
        this.mes = mes;
        this.cargos = cargos;
    }
}
