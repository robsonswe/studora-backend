package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidade que representa um concurso")
public class Concurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do concurso", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    @Schema(description = "Instituição organizadora do concurso")
    private Instituicao instituicao;

    @ManyToOne
    @JoinColumn(name = "banca_id", nullable = false)
    @Schema(description = "Banca organizadora do concurso")
    private Banca banca;

    @Column(nullable = false)
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023")
    private Integer ano;

    @OneToMany(
        mappedBy = "concurso",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Questões associadas ao concurso")
    private List<Questao> questoes;

    @OneToMany(
        mappedBy = "concurso",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Associações entre o concurso e cargos")
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
