package com.studora.dto.simulado;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.questao.QuestaoSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "DTO detalhado para um simulado e suas questões")
@Data
public class SimuladoDetailDto {

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

    @Schema(description = "ID da banca de preferência", example = "1")
    @JsonView(Views.Geracao.class)
    private Long bancaId;

    @Schema(description = "ID do cargo de preferência", example = "10")
    @JsonView(Views.Geracao.class)
    private Long cargoId;

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
    private java.util.List<SimuladoItemSelectionDto> disciplinas;

    @Schema(description = "Seleção de temas")
    @JsonView(Views.Geracao.class)
    private java.util.List<SimuladoItemSelectionDto> temas;

    @Schema(description = "Seleção de subtemas")
    @JsonView(Views.Geracao.class)
    private java.util.List<SimuladoItemSelectionDto> subtemas;

    @Schema(description = "Questões associadas ao simulado")
    @JsonView(Views.RespostaOculta.class)
    private List<QuestaoSummaryDto> questoes;
}