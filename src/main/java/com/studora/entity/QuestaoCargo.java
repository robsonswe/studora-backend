package com.studora.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
    name = "questao_cargo",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "questao_id", "concurso_cargo_id" }),
    },
    indexes = {
        @Index(
            name = "idx_questao_cargo_concurso_cargo",
            columnList = "concurso_cargo_id"
        ),
    }
)
public class QuestaoCargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne
    @JoinColumn(name = "concurso_cargo_id", nullable = false)
    private ConcursoCargo concursoCargo;
}
