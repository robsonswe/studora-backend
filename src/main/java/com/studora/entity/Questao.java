package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidade que representa uma questão de concurso")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da questão", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Concurso ao qual a questão pertence")
    private Concurso concurso;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Texto do enunciado da questão", example = "Qual é a capital do Brasil?")
    private String enunciado;

    @Column(nullable = false, columnDefinition = "INTEGER")
    @Schema(description = "Indica se a questão foi anulada", example = "false", defaultValue = "false")
    private Boolean anulada = false;

    @Column(name = "image_url", columnDefinition = "TEXT")
    @Schema(description = "URL da imagem associada à questão", example = "https://exemplo.com/imagem.jpg")
    private String imageUrl;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Alternativas associadas à questão")
    private List<Alternativa> alternativas = new java.util.ArrayList<>();

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Respostas associadas à questão")
    private List<Resposta> respostas = new java.util.ArrayList<>();

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
    @Schema(description = "Subtemas associados à questão")
    private List<Subtema> subtemas = new java.util.ArrayList<>();


    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Associações entre a questão e cargos do concurso")
    private List<QuestaoCargo> questaoCargos = new java.util.ArrayList<>();

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : null;
    }

    public List<QuestaoCargo> getQuestaoCargos() {
        return questaoCargos;
    }

    public void setQuestaoCargos(List<QuestaoCargo> questaoCargos) {
        this.questaoCargos = questaoCargos;
    }
}
