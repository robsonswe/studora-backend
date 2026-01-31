package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO para representar uma instituição")
@Data
public class InstituicaoDto {
    @Schema(description = "ID único da instituição (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome da instituição", example = "Universidade Federal do Rio de Janeiro")
    private String nome;

    @Schema(description = "Área da instituição", example = "Educação")
    private String area;
}
