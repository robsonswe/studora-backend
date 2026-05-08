package com.studora.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "secao_disciplina")
@Getter
@Setter
public class SecaoDisciplina extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secao_cargo_id", nullable = false)
    private SecaoCargo secaoCargo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(name = "num_questoes")
    private Integer numQuestoes;

    private Double peso;

    @Column(name = "nota_minima")
    private Double notaMinima;

    @ManyToMany
    @JoinTable(
        name = "secao_disciplina_subtema",
        joinColumns = @JoinColumn(name = "secao_disciplina_id"),
        inverseJoinColumns = @JoinColumn(name = "subtema_id")
    )
    private Set<Subtema> subtemas = new LinkedHashSet<>();
}
