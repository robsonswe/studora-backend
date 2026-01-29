package com.studora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubtemaDto {
    
    private Long id;
    
    @NotNull(message = "ID do tema é obrigatório")
    private Long temaId;
    
    @NotBlank(message = "Nome do subtema é obrigatório")
    @Size(max = 255, message = "Nome do subtema deve ter no máximo 255 caracteres")
    private String nome;
    
    // Constructors
    public SubtemaDto() {}
    
    public SubtemaDto(Long temaId, String nome) {
        this.temaId = temaId;
        this.nome = nome;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTemaId() {
        return temaId;
    }
    
    public void setTemaId(Long temaId) {
        this.temaId = temaId;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
}