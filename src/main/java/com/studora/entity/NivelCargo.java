package com.studora.entity;

public enum NivelCargo {
    FUNDAMENTAL("Fundamental"),
    MEDIO("Médio"),
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