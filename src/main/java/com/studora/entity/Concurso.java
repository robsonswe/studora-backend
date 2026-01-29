package com.studora.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "concurso")
public class Concurso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private String banca;
    
    @Column(nullable = false)
    private Integer ano;
    
    private String cargo;
    
    private String nivel;
    
    private String area;
    
    @OneToMany(mappedBy = "concurso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Questao> questoes;
    
    // Constructors
    public Concurso() {}
    
    public Concurso(String nome, String banca, Integer ano, String cargo, String nivel, String area) {
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
    
    public List<Questao> getQuestoes() {
        return questoes;
    }
    
    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }
}