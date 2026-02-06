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
    @JsonView(Views.RespostaOculta.class)
    private LocalDateTime startedAt;

    @Schema(description = "Data e hora de término", example = "2023-06-15T12:30:00")
    @JsonView(Views.RespostaOculta.class)
    private LocalDateTime finishedAt;

    @Schema(description = "Questões associadas ao simulado")
    @JsonView(Views.RespostaOculta.class)
    private List<QuestaoSummaryDto> questoes;
}