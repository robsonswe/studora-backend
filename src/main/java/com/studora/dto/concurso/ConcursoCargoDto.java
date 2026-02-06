package com.studora.dto.concurso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para representar a associação entre concurso e cargo")
@Data
public class ConcursoCargoDto {
    @Schema(description = "ID único da associação concurso-cargo (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID do concurso", example = "1")
    private Long concursoId;

    @Schema(description = "ID do cargo", example = "1")
    private Long cargoId;
}
