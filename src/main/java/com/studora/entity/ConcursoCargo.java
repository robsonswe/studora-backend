package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Objects;

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
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Concurso associado")
    private Concurso concurso;

    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    @Schema(description = "Cargo associado")
    private Cargo cargo;

    @OneToMany(
        mappedBy = "concursoCargo",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Associações entre este concurso-cargo e questões")
    @ToString.Exclude
    private java.util.Set<QuestaoCargo> questaoCargos = new java.util.LinkedHashSet<>();

    public void addQuestaoCargo(QuestaoCargo questaoCargo) {
        this.questaoCargos.add(questaoCargo);
        questaoCargo.setConcursoCargo(this);
    }

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
