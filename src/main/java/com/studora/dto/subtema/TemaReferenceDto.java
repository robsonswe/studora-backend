package com.studora.dto.subtema;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Schema(description = "Referência simplificada para tema")
public class TemaReferenceDto {
    @Schema(description = "ID do tema", example = "1")
    private Long id;

    @Schema(description = "Nome do tema", example = "Direitos Fundamentais")
    private String nome;
}
