package com.studora.dto.simulado;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Representa a seleção de um item (Disciplina, Tema ou Subtema) e sua quantidade em um simulado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimuladoItemSelectionDto {
    
    @Schema(description = "ID do item (Disciplina, Tema ou Subtema)", example = "1")
    private Long id;

    @Schema(description = "Quantidade de questões desejadas para este ID", example = "10")
    private int quantidade;
}
