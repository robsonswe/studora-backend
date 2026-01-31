package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
    name = "concurso_cargo",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "concurso_id", "cargo_id" }),
    },
    indexes = {
        @Index(
            name = "idx_concurso_cargo_concurso",
            columnList = "concurso_id"
        ),
        @Index(name = "idx_concurso_cargo_cargo", columnList = "cargo_id"),
    }
)
@Schema(description = "Entidade que representa a associação entre concurso e cargo")
public class ConcursoCargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da associação concurso-cargo", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concurso_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Concurso associado")
    private Concurso concurso;

    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    @Schema(description = "Cargo associado")
    private Cargo cargo;
}
