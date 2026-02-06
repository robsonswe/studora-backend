package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "disciplina")
@Schema(description = "Entidade que representa uma disciplina")
public class Disciplina extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da disciplina", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nome da disciplina", example = "Matemática")
    private String nome;

    // Constructors
    public Disciplina() {}

    public Disciplina(String nome) {
        this.nome = nome;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
