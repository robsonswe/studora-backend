package com.studora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ConcursoDto {
    
    private Long id;
    
    @NotBlank(message = "Nome do concurso é obrigatório")
    @Size(max = 255, message = "Nome do concurso deve ter no máximo 255 caracteres")
    private String nome;
    
    @NotBlank(message = "Banca é obrigatória")
    @Size(max = 100, message = "Banca deve ter no máximo 100 caracteres")
    private String banca;
    
    @NotNull(message = "Ano é obrigatório")
    @Positive(message = "Ano deve ser um número positivo")
    private Integer ano;
    
    @Size(max = 255, message = "Cargo deve ter no máximo 255 caracteres")
    private String cargo;
    
    @Size(max = 100, message = "Nível deve ter no máximo 100 caracteres")
    private String nivel;
    
    @Size(max = 100, message = "Área deve ter no máximo 100 caracteres")
    private String area;
    
    // Constructors
    public ConcursoDto() {}
    
    public ConcursoDto(String nome, String banca, Integer ano, String cargo, String nivel, String area) {
        this.nome = nome;
        this.banca = banca;
        this.ano = ano;
        this.cargo = cargo;
        this.nivel = nivel;
        this.area = area;
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
    
    public String getBanca() {
        return banca;
    }
    
    public void setBanca(String banca) {
        this.banca = banca;
    }
    
    public Integer getAno() {
        return ano;
    }
    
    public void setAno(Integer ano) {
        this.ano = ano;
    }
    
    public String getCargo() {
        return cargo;
    }
    
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
    
    public String getNivel() {
        return nivel;
    }
    
    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
    }
}