package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "concurso",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = { "instituicao_id", "banca_id", "ano", "mes" }
        ),
    },
    indexes = {
        @Index(
            name = "idx_concurso_instituicao",
            columnList = "instituicao_id"
        ),
        @Index(name = "idx_concurso_banca", columnList = "banca_id"),
        @Index(name = "idx_concurso_ano", columnList = "ano"),
        @Index(name = "idx_concurso_mes", columnList = "mes"),
    }
)
@Schema(description = "Entidade que representa um concurso")
public class Concurso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do concurso", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    @Schema(description = "Instituição organizadora do concurso")
    private Instituicao instituicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banca_id", nullable = false)
    @Schema(description = "Banca organizadora do concurso")
    private Banca banca;

    @Column(nullable = false)
    @Min(value = AppConstants.MIN_YEAR, message = "Ano deve ser no mínimo " + AppConstants.MIN_YEAR)
    @Max(value = AppConstants.MAX_YEAR, message = "Ano deve ser no máximo " + AppConstants.MAX_YEAR)
    @Schema(description = "Ano em que o concurso foi realizado", example = "2023")
    private Integer ano;

    @Column(nullable = false)
    @Min(value = AppConstants.MIN_MONTH, message = "Mês deve ser entre " + AppConstants.MIN_MONTH + " e " + AppConstants.MAX_MONTH)
    @Max(value = AppConstants.MAX_MONTH, message = "Mês deve ser entre " + AppConstants.MIN_MONTH + " e " + AppConstants.MAX_MONTH)
    @Schema(description = "Mês em que o concurso foi realizado", example = "6")
    private Integer mes;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Identificação do edital do concurso", example = "Edital 01/2023")
    private String edital;

    @OneToMany(
        mappedBy = "concurso",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Questões associadas ao concurso")
    private Set<Questao> questoes = new LinkedHashSet<>();

    @OneToMany(
        mappedBy = "concurso",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Associações entre o concurso e cargos")
    private Set<ConcursoCargo> concursoCargos = new LinkedHashSet<>();

    public Concurso() {}

    public Concurso(Instituicao instituicao, Banca banca, Integer ano, Integer mes) {
        this.instituicao = instituicao;
        this.banca = banca;
        this.ano = ano;
        this.mes = mes;
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

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public String getEdital() {
        return edital;
    }

    public void setEdital(String edital) {
        this.edital = edital;
    }

    public Set<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(Set<Questao> questoes) {
        this.questoes = questoes;
    }

    public Set<ConcursoCargo> getConcursoCargos() {
        return concursoCargos;
    }

    public void setConcursoCargos(Set<ConcursoCargo> concursoCargos) {
        this.concursoCargos = concursoCargos;
    }

    public void addQuestao(Questao questao) {
        this.questoes.add(questao);
        questao.setConcurso(this);
    }

    public void addConcursoCargo(ConcursoCargo concursoCargo) {
        this.concursoCargos.add(concursoCargo);
        concursoCargo.setConcurso(this);
    }
}
