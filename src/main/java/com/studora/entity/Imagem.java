package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "imagem")
@Schema(description = "Entidade que representa uma imagem")
public class Imagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da imagem", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "URL da imagem", example = "https://exemplo.com/imagem.jpg")
    private String url;

    @Schema(description = "Descrição da imagem", example = "Gráfico mostrando crescimento populacional")
    private String descricao; // Description of the image

    @ManyToMany(mappedBy = "imagens", fetch = FetchType.LAZY)
    @Schema(description = "Questões associadas à imagem")
    private List<Questao> questoes;

    @ManyToMany(mappedBy = "imagens", fetch = FetchType.LAZY)
    @Schema(description = "Alternativas associadas à imagem")
    private List<Alternativa> alternativas;

    // Constructors
    public Imagem() {}

    public Imagem(String url, String descricao) {
        this.url = url;
        this.descricao = descricao;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<Questao> questoes) {
        this.questoes = questoes;
    }

    public List<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }
}