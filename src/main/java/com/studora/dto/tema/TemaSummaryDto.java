package com.studora.dto.tema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de temas")
@Data
public class TemaSummaryDto {
    private Long id;
    private Long disciplinaId;
    private String nome;
}
