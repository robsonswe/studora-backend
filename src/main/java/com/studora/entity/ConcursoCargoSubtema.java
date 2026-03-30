package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@ToString
@Entity
@Table(
    name = "concurso_cargo_subtema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "concurso_cargo_id", "subtema_id" }),
    },
    indexes = {
        @Index(name = "idx_concurso_cargo_subtema_concurso_cargo", columnList = "concurso_cargo_id"),
    }
)
@Schema(description = "Entidade que representa a associação entre concurso-cargo e subtema")
public class ConcursoCargoSubtema extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da associação concurso-cargo-subtema", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concurso_cargo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "Associação concurso-cargo associada")
    @ToString.Exclude
    private ConcursoCargo concursoCargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtema_id", nullable = false)
    @Schema(description = "Subtema associado")
    private Subtema subtema;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcursoCargoSubtema)) return false;
        ConcursoCargoSubtema that = (ConcursoCargoSubtema) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
