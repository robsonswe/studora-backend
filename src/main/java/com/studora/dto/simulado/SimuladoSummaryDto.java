package com.studora.dto.simulado;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "DTO resumido para listagem de simulados")
@Data
public class SimuladoSummaryDto {

    @Schema(description = "ID único do simulado", example = "1")
    private Long id;

    @Schema(description = "Nome do simulado", example = "Simulado PC-SP 2024")
    private String nome;

    @Schema(description = "Data e hora de início", example = "2023-06-15T10:30:00")
    private LocalDateTime startedAt;

    @Schema(description = "Data e hora de término", example = "2023-06-15T12:30:00")
    private LocalDateTime finishedAt;
}
