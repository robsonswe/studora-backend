package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Grau de dificuldade percebido pelo usuário na questão", enumAsRef = true)
public enum Dificuldade {
    @Schema(description = "1 - Fácil")
    FACIL(1, "Fácil"),
    
    @Schema(description = "2 - Média")
    MEDIA(2, "Média"),
    
    @Schema(description = "3 - Difícil")
    DIFICIL(3, "Difícil"),
    
    @Schema(description = "4 - Chute (Não tenho certeza)")
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
