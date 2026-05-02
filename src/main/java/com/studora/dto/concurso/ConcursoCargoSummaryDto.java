package com.studora.dto.concurso;

import com.studora.entity.NivelCargo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO que representa a associação de um cargo a um concurso com status de inscrição")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcursoCargoSummaryDto {
    @Schema(description = "ID da associação concurso-cargo", example = "1")
    private Long id;

    @Schema(description = "ID do cargo", example = "1")
    private Long cargoId;

    @Schema(description = "Nome do cargo", example = "Agente")
    private String cargoNome;

    @Schema(description = "Nível de escolaridade do cargo")
    private NivelCargo nivel;

    @Schema(description = "Área de atuação do cargo", example = "Policial")
    private String area;

    @Schema(description = "Indica se o usuário está inscrito para este cargo neste concurso")
    private boolean inscrito;

    @Schema(description = "Provas associadas a este cargo neste concurso")
    private java.util.List<ConcursoProvaDto> provas;

    @Schema(description = "Seções (tópicos do edital) associadas a este cargo neste concurso")
    private java.util.List<ConcursoSecaoDto> topicos;
}
