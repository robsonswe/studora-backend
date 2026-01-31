package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "DTO para representar um concurso")
@Data
public class ConcursoDto {

    @Schema(description = "ID único do concurso (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "ID da instituição é obrigatório")
    @Schema(description = "ID da instituição organizadora do concurso", example = "1", required = true)
    private Long instituicaoId;

    @NotNull(message = "ID da banca é obrigatório")
    @Schema(description = "ID da banca organizadora do concurso", example = "1", required = true)
    private Long bancaId;

    @NotNull(message = "Ano é obrigatório")
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023", required = true)
    private Integer ano;

    @NotNull(message = "Mês é obrigatório")
    @Schema(description = "Mês em que o concurso foi realizado", example = "6", required = true)
    private Integer mes;

    @Schema(description = "Identificação do edital do concurso", example = "Edital 01/2023")
    private String edital;

    // Constructors
    public ConcursoDto() {}

    public ConcursoDto(Long instituicaoId, Long bancaId, Integer ano, Integer mes) {
        this.instituicaoId = instituicaoId;
        this.bancaId = bancaId;
        this.ano = ano;
        this.mes = mes;
    }
}
