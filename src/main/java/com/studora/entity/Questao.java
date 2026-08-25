package com.studora.entity;

import java.util.LinkedHashSet;
import java.util.Set;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "questao",
    indexes = {
        @Index(name = "idx_questao_anulada", columnList = "anulada"),
        @Index(name = "idx_questao_autoral", columnList = "autoral"),
    }
)
@Schema(description = "Entidade que representa uma questão de um concurso")
public class Questao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da questão", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?")
    private String enunciado;

    @Column(nullable = false)
    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    private Boolean anulada = false;

    @Column(nullable = false)
    @Schema(description = "Indica se a questão é autoral (independente de concurso/cargo)", example = "false", defaultValue = "false")
    private Boolean autoral = false;

    @Column(name = "image_url")
    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    private String imageUrl;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Alternativas associadas à questão")
    private Set<Alternativa> alternativas = new LinkedHashSet<>();

    @Column(nullable = false)
    @Schema(description = "Indica se a questão está desatualizada", example = "false", defaultValue = "false")
    private Boolean desatualizada = false;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Respostas associadas à questão")
    private Set<Resposta> respostas = new LinkedHashSet<>();

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Relacionamentos com subtemas da questão")
    private Set<QuestaoSubtema> questaoSubtemas = new LinkedHashSet<>();


    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Seções de prova às quais a questão pertence")
    private Set<QuestaoProvaSecao> secoes = new LinkedHashSet<>();

    // Constructors
    public Questao() {}

    public Questao(String enunciado) {
        this.enunciado = enunciado;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    public Boolean getAutoral() {
        return autoral;
    }

    public void setAutoral(Boolean autoral) {
        this.autoral = autoral;
    }

    public Set<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(Set<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }

    public Boolean getDesatualizada() {
        return desatualizada;
    }

    public void setDesatualizada(Boolean desatualizada) {
        this.desatualizada = desatualizada;
    }

    public Set<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(Set<Resposta> respostas) {
        this.respostas = respostas;
    }

    public Set<QuestaoSubtema> getQuestaoSubtemas() {
        return questaoSubtemas;
    }

    public void setQuestaoSubtemas(Set<QuestaoSubtema> questaoSubtemas) {
        this.questaoSubtemas = questaoSubtemas;
    }

    public void addSubtema(Subtema subtema, boolean principal) {
        QuestaoSubtema qs = new QuestaoSubtema(this, subtema, principal);
        this.questaoSubtemas.add(qs);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : null;
    }

    public Set<QuestaoProvaSecao> getSecoes() {
        return secoes;
    }

    public void setSecoes(Set<QuestaoProvaSecao> secoes) {
        this.secoes = secoes;
    }

    public void addSecao(QuestaoProvaSecao questaoProvaSecao) {
        this.secoes.add(questaoProvaSecao);
        questaoProvaSecao.setQuestao(this);
    }
}
