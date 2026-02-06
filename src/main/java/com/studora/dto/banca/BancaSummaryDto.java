package com.studora.dto.banca;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de bancas")
@Data
public class BancaSummaryDto {
    private Long id;
    private String nome;
}
