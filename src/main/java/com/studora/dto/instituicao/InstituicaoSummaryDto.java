package com.studora.dto.instituicao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de instituições")
@Data
public class InstituicaoSummaryDto {
    private Long id;
    private String nome;
    private String area;
}
