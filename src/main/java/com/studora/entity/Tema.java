package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidade que representa um tema dentro de uma disciplina")
public class Tema extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do tema", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    @Schema(description = "Disciplina à qual o tema pertence")
    private Disciplina disciplina;

    @Column(nullable = false)
    @Schema(description = "Nome do tema", example = "Álgebra")
    private String nome;

    @OneToMany(mappedBy = "tema", fetch = FetchType.LAZY)
    @Schema(description = "Subtemas associados ao tema")
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
