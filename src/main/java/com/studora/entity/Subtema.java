package com.studora.entity;

import com.studora.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "subtema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tema_id", "nome" }),
    },
    indexes = { @Index(name = "idx_subtema_tema", columnList = "tema_id") }
)
@Schema(description = "Entidade que representa um subtema dentro de um tema")
public class Subtema extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do subtema", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    @Schema(description = "Tema ao qual o subtema pertence")
    private Tema tema;

    @Column(nullable = false)
    @Schema(description = "Nome do subtema", example = "Equações de primeiro grau")
    private String nome;

    @Column(name = "nome_normalized")
    @Schema(hidden = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String nomeNormalized;

    @PrePersist
    @PreUpdate
    public void normalize() {
        this.nomeNormalized = StringUtils.normalizeForSearch(this.nome);
    }

    @OneToMany(mappedBy = "subtema", fetch = FetchType.LAZY)
    @Schema(description = "Relações entre questões e este subtema")
    private Set<QuestaoSubtema> questaoSubtemas = new LinkedHashSet<>();

    // Constructors
    public Subtema() {}

    public Subtema(Tema tema, String nome) {
        this.tema = tema;
        this.nome = nome;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeNormalized() {
        return nomeNormalized;
    }

    public void setNomeNormalized(String nomeNormalized) {
        this.nomeNormalized = nomeNormalized;
    }

    public Set<QuestaoSubtema> getQuestaoSubtemas() {
        return questaoSubtemas;
    }

    public void setQuestaoSubtemas(Set<QuestaoSubtema> questaoSubtemas) {
        this.questaoSubtemas = questaoSubtemas;
    }
}
