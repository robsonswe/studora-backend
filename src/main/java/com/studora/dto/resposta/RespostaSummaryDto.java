package com.studora.dto.resposta;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para resumo de resposta")
public class RespostaSummaryDto {
    @Schema(description = "ID único da resposta", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "ID da questão respondida", example = "1")
    @JsonView(Views.Summary.class)
    private Long questaoId;

    @Schema(description = "ID da alternativa selecionada", example = "1")
    @JsonView(Views.Summary.class)
    private Long alternativaId;

    @Schema(description = "Indica se a resposta foi correta", example = "true")
    @JsonView(Views.Summary.class)
    private Boolean correta;

    @Schema(description = "Justificativa da resposta", example = "Raciocínio lógico...")
    @JsonView(Views.Summary.class)
    private String justificativa;

    @Schema(description = "Grau de dificuldade percebido pelo usuário")
    @JsonView(Views.Summary.class)
    private com.studora.entity.Dificuldade dificuldade;

    @Schema(description = "Tempo levado para responder em segundos", example = "45")
    @JsonView(Views.Summary.class)
    private Integer tempoRespostaSegundos;

    @Schema(description = "ID do simulado ao qual a resposta pertence (opcional)", example = "1")
    @JsonView(Views.Summary.class)
    private Long simuladoId;

    @Schema(description = "Data e hora em que a resposta foi registrada", example = "2023-06-15T10:30:00")
    @JsonView(Views.Summary.class)
    private LocalDateTime createdAt;
}
