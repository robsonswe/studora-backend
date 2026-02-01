package com.studora.entity;

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

    @ManyToMany(mappedBy = "subtemas", fetch = FetchType.LAZY)
    @Schema(description = "Questões associadas ao subtema")
    private Set<Questao> questoes = new LinkedHashSet<>();

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

    public Set<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(Set<Questao> questoes) {
        this.questoes = questoes;
    }
}
