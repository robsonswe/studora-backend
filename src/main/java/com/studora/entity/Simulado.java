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

    @Column(name = "banca_id")
    @Schema(description = "ID da banca de preferência usada na geração")
    private Long bancaId;

    @Column(name = "cargo_id")
    @Schema(description = "ID do cargo de preferência usado na geração")
    private Long cargoId;

    @ElementCollection
    @CollectionTable(name = "simulado_area", joinColumns = @JoinColumn(name = "simulado_id"))
    @Column(name = "area")
    @Schema(description = "Áreas de preferência usadas na geração")
    private java.util.List<String> areas;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    @Schema(description = "Nível do cargo usado como teto na geração")
    private NivelCargo nivel;

    @Column(name = "ignorar_respondidas")
    @Schema(description = "Se questões respondidas foram ignoradas na geração")
    private Boolean ignorarRespondidas;

    @ElementCollection
    @CollectionTable(name = "simulado_disciplina", joinColumns = @JoinColumn(name = "simulado_id"))
    @Schema(description = "Seleção de disciplinas usadas na geração")
    private java.util.List<SimuladoItemSelection> disciplinas;

    @ElementCollection
    @CollectionTable(name = "simulado_tema", joinColumns = @JoinColumn(name = "simulado_id"))
    @Schema(description = "Seleção de temas usados na geração")
    private java.util.List<SimuladoItemSelection> temas;

    @ElementCollection
    @CollectionTable(name = "simulado_subtema", joinColumns = @JoinColumn(name = "simulado_id"))
    @Schema(description = "Seleção de subtemas usados na geração")
    private java.util.List<SimuladoItemSelection> subtemas;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "simulado_questao",
        joinColumns = @JoinColumn(name = "simulado_id"),
        inverseJoinColumns = @JoinColumn(name = "questao_id")
    )
    @OrderColumn(name = "ordem")
    @Schema(description = "Questões que compõem este simulado")
    private java.util.List<Questao> questoes = new java.util.ArrayList<>();

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
