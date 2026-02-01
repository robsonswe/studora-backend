package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidade que representa a associação entre uma questão e um cargo")
public class QuestaoCargo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da associação questão-cargo", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "questao_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Questão associada")
    private Questao questao;

    @ManyToOne
    @JoinColumn(name = "concurso_cargo_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Associação concurso-cargo associada")
    private ConcursoCargo concursoCargo;
}
