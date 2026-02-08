package com.studora.dto.banca;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de bancas")
@Data
public class BancaSummaryDto {
    @Schema(description = "ID único da banca", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;

    @Schema(description = "Nome da banca organizadora", example = "Cebraspe (CESPE)")
    @JsonView(Views.Summary.class)
    private String nome;
}
