package com.studora.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@ToString
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
public class ConcursoCargo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da associação concurso-cargo", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concurso_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "Concurso associado")
    private Concurso concurso;

    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    @Schema(description = "Cargo associado")
    private Cargo cargo;

    @Column(nullable = false)
    @Schema(description = "Indica se o usuário está inscrito para este cargo neste concurso", example = "false")
    private boolean inscrito = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcursoCargo)) return false;
        ConcursoCargo that = (ConcursoCargo) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
