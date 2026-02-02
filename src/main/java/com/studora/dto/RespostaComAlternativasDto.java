package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "DTO para representar uma resposta a uma questão com alternativas")
@Data
public class RespostaComAlternativasDto {

    @Schema(description = "ID único da resposta (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID da questão respondida", example = "1", required = true)
    private Long questaoId;

    @Schema(description = "ID da alternativa selecionada como resposta", example = "1", required = true)
    private Long alternativaId;

    @Schema(description = "Indica se a alternativa selecionada é a correta", example = "true")
    private Boolean correta;

    @Schema(description = "Data e hora em que a resposta foi registrada (gerada automaticamente)", example = "2023-06-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Lista de alternativas associadas à questão", implementation = AlternativaDto.class)
    private List<AlternativaDto> alternativas;

    // Constructors
    public RespostaComAlternativasDto() {}
}