package com.studora.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "subtema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tema_id", "nome" }),
    },
    indexes = { @Index(name = "idx_subtema_tema", columnList = "tema_id") }
)
public class Subtema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @Column(nullable = false)
    private String nome;

    @ManyToMany(mappedBy = "subtemas", fetch = FetchType.LAZY)
    private List<Questao> questoes;

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

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }
}
