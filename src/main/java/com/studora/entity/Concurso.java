package com.studora.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "concurso",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = { "instituicao_id", "banca_id", "ano" }
        ),
    },
    indexes = {
        @Index(
            name = "idx_concurso_instituicao",
            columnList = "instituicao_id"
        ),
        @Index(name = "idx_concurso_banca", columnList = "banca_id"),
        @Index(name = "idx_concurso_ano", columnList = "ano"),
    }
)
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    @ManyToOne
    @JoinColumn(name = "banca_id", nullable = false)
    private Banca banca;

    @Column(nullable = false)
    private Integer ano;

    @OneToMany(
        mappedBy = "concurso",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<Questao> questoes;

    @OneToMany(
        mappedBy = "concurso",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<ConcursoCargo> concursoCargos;

    public Concurso() {}

    public Concurso(Instituicao instituicao, Banca banca, Integer ano) {
        this.instituicao = instituicao;
        this.banca = banca;
        this.ano = ano;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public Banca getBanca() {
        return banca;
    }

    public void setBanca(Banca banca) {
        this.banca = banca;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }

    public List<ConcursoCargo> getConcursoCargos() {
        return concursoCargos;
    }

    public void setConcursoCargos(List<ConcursoCargo> concursoCargos) {
        this.concursoCargos = concursoCargos;
    }
}
