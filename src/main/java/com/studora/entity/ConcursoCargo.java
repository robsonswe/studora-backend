package com.studora.entity;

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
public class ConcursoCargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concurso_id", nullable = false)
    private Concurso concurso;

    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;
}
