package com.studora.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "questao",
    indexes = {
        @Index(name = "idx_questao_concurso", columnList = "concurso_id"),
        @Index(name = "idx_questao_anulada", columnList = "anulada"),
    }
)
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_id", nullable = false)
    private Concurso concurso;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(nullable = false, columnDefinition = "INTEGER")
    private Boolean anulada = false;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<Alternativa> alternativas;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<Resposta> respostas;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "questao_subtema",
        joinColumns = @JoinColumn(name = "questao_id"),
        inverseJoinColumns = @JoinColumn(name = "subtema_id"),
        indexes = {
            @Index(
                name = "idx_questao_subtema_subtema",
                columnList = "subtema_id"
            ),
        }
    )
    private List<Subtema> subtemas;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "questao_imagem",
        joinColumns = @JoinColumn(name = "questao_id"),
        inverseJoinColumns = @JoinColumn(name = "imagem_id"),
        indexes = {
            @Index(
                name = "idx_questao_imagem_imagem",
                columnList = "imagem_id"
            ),
        }
    )
    private List<Imagem> imagens;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<QuestaoCargo> questaoCargos;

    // Constructors
    public Questao() {}

    public Questao(Concurso concurso, String enunciado) {
        this.concurso = concurso;
        this.enunciado = enunciado;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Concurso getConcurso() {
        return concurso;
    }

    public void setConcurso(Concurso concurso) {
        this.concurso = concurso;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    public List<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }

    public List<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<Resposta> respostas) {
        this.respostas = respostas;
    }

    public List<Subtema> getSubtemas() {
        return subtemas;
    }

    public void setSubtemas(List<Subtema> subtemas) {
        this.subtemas = subtemas;
    }

    public List<Imagem> getImagens() {
        return imagens;
    }

    public void setImagens(List<Imagem> imagens) {
        this.imagens = imagens;
    }

    public List<QuestaoCargo> getQuestaoCargos() {
        return questaoCargos;
    }

    public void setQuestaoCargos(List<QuestaoCargo> questaoCargos) {
        this.questaoCargos = questaoCargos;
    }
}
