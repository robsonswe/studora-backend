package com.studora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DisciplinaDto {
    
    private Long id;
    
    @NotBlank(message = "Nome da disciplina é obrigatório")
    @Size(max = 255, message = "Nome da disciplina deve ter no máximo 255 caracteres")
    private String nome;
    
    // Constructors
    public DisciplinaDto() {}
    
    public DisciplinaDto(String nome) {
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