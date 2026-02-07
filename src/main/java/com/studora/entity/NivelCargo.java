package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de escolaridade exigido para o cargo", enumAsRef = true)
public enum NivelCargo {
    @Schema(description = "Ensino Fundamental")
    FUNDAMENTAL("Fundamental"),
    
    @Schema(description = "Ensino Médio")
    MEDIO("Médio"),
    
    @Schema(description = "Ensino Superior")
    SUPERIOR("Superior");

    private final String descricao;

    NivelCargo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static NivelCargo fromDescricao(String descricao) {
        for (NivelCargo nivel : NivelCargo.values()) {
            if (nivel.descricao.equalsIgnoreCase(descricao)) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Nível inválido: " + descricao);
    }
}
