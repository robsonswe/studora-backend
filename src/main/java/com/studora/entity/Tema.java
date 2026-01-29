package com.studora.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "tema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "disciplina_id", "nome" })
    },
    indexes = {
        @Index(name = "idx_tema_disciplina", columnList = "disciplina_id")
    }
)
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @Column(nullable = false)
    private String nome;

    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subtema> subtemas;

    // Constructors
    public Tema() {}

    public Tema(Disciplina disciplina, String nome) {
        this.disciplina = disciplina;
        this.nome = nome;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Subtema> getSubtemas() {
        return subtemas;
    }

    public void setSubtemas(List<Subtema> subtemas) {
        this.subtemas = subtemas;
    }
}
