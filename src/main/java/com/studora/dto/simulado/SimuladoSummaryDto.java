package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "DTO resumido para listagem de simulados")
@Data
public class SimuladoSummaryDto {

    @Schema(description = "ID único do simulado", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome do simulado", example = "Simulado PC-SP 2024")
    @JsonView(Views.Summary.class)
    private String nome;

    @Schema(description = "Data e hora de início", example = "2023-06-15T10:30:00")
    @JsonView(Views.Summary.class)
    private LocalDateTime startedAt;

    @Schema(description = "Data e hora de término", example = "2023-06-15T12:30:00")
    @JsonView(Views.Summary.class)
    private LocalDateTime finishedAt;

    @Schema(description = "Banca de preferência")
    @JsonView(Views.Geracao.class)
    private com.studora.dto.banca.BancaSummaryDto banca;

    @Schema(description = "Cargo de preferência")
    @JsonView(Views.Geracao.class)
    private com.studora.dto.cargo.CargoSummaryDto cargo;

    @Schema(description = "Lista de áreas de preferência", example = "[\"Jurídica\"]")
    @JsonView(Views.Geracao.class)
    private java.util.List<String> areas;

    @Schema(description = "Nível do cargo", example = "SUPERIOR")
    @JsonView(Views.Geracao.class)
    private com.studora.entity.NivelCargo nivel;

    @Schema(description = "Se ignorou questões respondidas", example = "false")
    @JsonView(Views.Geracao.class)
    private Boolean ignorarRespondidas;

    @Schema(description = "Seleção de disciplinas")
    @JsonView(Views.Geracao.class)
    private java.util.List<DisciplinaSimuladoDto> disciplinas;

    @Schema(description = "Seleção de temas")
    @JsonView(Views.Geracao.class)
    private java.util.List<TemaSimuladoDto> temas;

    @Schema(description = "Seleção de subtemas")
    @JsonView(Views.Geracao.class)
    private java.util.List<SubtemaSimuladoDto> subtemas;
}
