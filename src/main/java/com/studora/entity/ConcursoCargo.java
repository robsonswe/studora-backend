package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

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

    @OneToMany(
        mappedBy = "concursoCargo",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Associações entre este concurso-cargo e questões")
    @ToString.Exclude
    private Set<QuestaoCargo> questaoCargos = new LinkedHashSet<>();

    public void addQuestaoCargo(QuestaoCargo questaoCargo) {
        this.questaoCargos.add(questaoCargo);
        questaoCargo.setConcursoCargo(this);
    }

    @OneToMany(
        mappedBy = "concursoCargo",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Subtemas associados a este concurso-cargo")
    @ToString.Exclude
    private Set<ConcursoCargoSubtema> concursoCargoSubtemas = new LinkedHashSet<>();

    public void addConcursoCargoSubtema(ConcursoCargoSubtema concursoCargoSubtema) {
        this.concursoCargoSubtemas.add(concursoCargoSubtema);
        concursoCargoSubtema.setConcursoCargo(this);
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
