package com.studora.dto.concurso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de concursos")
@Data
public class ConcursoSummaryDto {
    @Schema(description = "ID único do concurso", example = "1")
    private Long id;

    @Schema(description = "Instituição organizadora")
    private com.studora.dto.instituicao.InstituicaoSummaryDto instituicao;

    @Schema(description = "Banca organizadora")
    private com.studora.dto.banca.BancaSummaryDto banca;

    @Schema(description = "Ano de realização do concurso", example = "2023")
    private Integer ano;

    @Schema(description = "Mês de realização do concurso", example = "5")
    private Integer mes;

    @Schema(description = "Identificação do edital do concurso", example = "https://exemplo.com/edital.pdf")
    private String edital;

    @Schema(description = "Lista de cargos associados ao concurso")
    private java.util.List<com.studora.dto.cargo.CargoSummaryDto> cargos;
}
