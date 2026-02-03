package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "simulado")
@Getter
@Setter
@ToString(exclude = "questoes")
@Schema(description = "Entidade que representa um simulado (conjunto de questões para estudo formal)")
public class Simulado extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do simulado", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nome ou título do simulado", example = "Simulado PC-SP 2024")
    private String nome;

    @Column(name = "started_at")
    @Schema(description = "Data e hora de início do simulado")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    @Schema(description = "Data e hora de término do simulado")
    private LocalDateTime finishedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "simulado_questao",
        joinColumns = @JoinColumn(name = "simulado_id"),
        inverseJoinColumns = @JoinColumn(name = "questao_id")
    )
    @Schema(description = "Questões que compõem este simulado")
    private Set<Questao> questoes = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Simulado)) return false;
        Simulado other = (Simulado) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
