package com.studora.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Dificuldade {
    FACIL(1, "Fácil"),
    MEDIA(2, "Média"),
    DIFICIL(3, "Difícil"),
    CHUTE(4, "Chute");

    private final int id;
    private final String descricao;

    public static Dificuldade fromId(int id) {
        for (Dificuldade d : Dificuldade.values()) {
            if (d.id == id) {
                return d;
            }
        }
        throw new IllegalArgumentException("ID de dificuldade inválido: " + id);
    }
}
