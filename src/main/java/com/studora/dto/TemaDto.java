package com.studora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TemaDto {
    
    private Long id;
    
    @NotNull(message = "ID da disciplina é obrigatório")
    private Long disciplinaId;
    
    @NotBlank(message = "Nome do tema é obrigatório")
    @Size(max = 255, message = "Nome do tema deve ter no máximo 255 caracteres")
    private String nome;
    
    // Constructors
    public TemaDto() {}
    
    public TemaDto(Long disciplinaId, String nome) {
        this.disciplinaId = disciplinaId;
        this.nome = nome;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDisciplinaId() {
        return disciplinaId;
    }
    
    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
}