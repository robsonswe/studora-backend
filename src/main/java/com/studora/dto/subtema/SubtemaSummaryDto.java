package com.studora.dto.subtema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de subtemas")
@Data
public class SubtemaSummaryDto {
    private Long id;
    private Long temaId;
    private String nome;
}
