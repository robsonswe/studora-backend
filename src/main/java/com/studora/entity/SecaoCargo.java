package com.studora.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "secao_cargo")
@Getter
@Setter
public class SecaoCargo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_cargo_id", nullable = false)
    private ConcursoCargo concursoCargo;

    @Column(nullable = false)
    private Double peso = 1.0;

    @Column(name = "nota_minima")
    private Double notaMinima;

    private Integer ordem;

    @Column(name = "num_questoes")
    private Integer numQuestoes;

    @ManyToMany
    @JoinTable(
        name = "secao_cargo_subtema",
        joinColumns = @JoinColumn(name = "secao_cargo_id"),
        inverseJoinColumns = @JoinColumn(name = "subtema_id")
    )
    private Set<Subtema> subtemas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "secaoCargo", cascade = CascadeType.ALL)
    private Set<ProvaSecao> provaSecoes = new LinkedHashSet<>();

    public void addSubtema(Subtema subtema) {
        this.subtemas.add(subtema);
    }
}
