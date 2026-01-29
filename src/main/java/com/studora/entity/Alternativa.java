package com.studora.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "alternativa")
public class Alternativa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;
    
    @Column(nullable = false)
    private Integer ordem; // Order of the alternative
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto; // Text of the alternative
    
    @Column(nullable = false)
    private Boolean correta; // Indicates if the alternative is correct
    
    @Column(columnDefinition = "TEXT")
    private String justificativa; // Justification for the alternative
    
    @OneToMany(mappedBy = "alternativaEscolhida", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Resposta> respostas;
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "alternativa_imagem",
        joinColumns = @JoinColumn(name = "alternativa_id"),
        inverseJoinColumns = @JoinColumn(name = "imagem_id")
    )
    private List<Imagem> imagens;
    
    // Constructors
    public Alternativa() {}
    
    public Alternativa(Questao questao, Integer ordem, String texto, Boolean correta) {
        this.questao = questao;
        this.ordem = ordem;
        this.texto = texto;
        this.correta = correta;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Questao getQuestao() {
        return questao;
    }
    
    public void setQuestao(Questao questao) {
        this.questao = questao;
    }
    
    public Integer getOrdem() {
        return ordem;
    }
    
    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
    
    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    public Boolean getCorreta() {
        return correta;
    }
    
    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }
    
    public String getJustificativa() {
        return justificativa;
    }
    
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
    
    public List<Resposta> getRespostas() {
        return respostas;
    }
    
    public void setRespostas(List<Resposta> respostas) {
        this.respostas = respostas;
    }
    
    public List<Imagem> getImagens() {
        return imagens;
    }
    
    public void setImagens(List<Imagem> imagens) {
        this.imagens = imagens;
    }
}