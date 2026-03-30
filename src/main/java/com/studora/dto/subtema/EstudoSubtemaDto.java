package com.studora.dto.subtema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "DTO que representa uma sessão de estudo de um subtema")
@Data
public class EstudoSubtemaDto {
    @Schema(description = "ID único da sessão de estudo", example = "1")
    private Long id;

    @Schema(description = "ID do subtema estudado", example = "1")
    private Long subtemaId;

    @Schema(description = "Data e hora em que o estudo foi realizado")
    private LocalDateTime createdAt;
}
