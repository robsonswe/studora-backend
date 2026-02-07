package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "questao",
    indexes = {
        @Index(name = "idx_questao_concurso", columnList = "concurso_id"),
        @Index(name = "idx_questao_anulada", columnList = "anulada"),
    }
)
@Schema(description = "Entidade que representa uma questão de um concurso")
public class Questao extends BaseEntity {

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
    private Set<Alternativa> alternativas = new LinkedHashSet<>();

    @Column(nullable = false, columnDefinition = "INTEGER")
    @Schema(description = "Indica se a questão está desatualizada", example = "false", defaultValue = "false")
    private Boolean desatualizada = false;

    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Respostas associadas à questão")
    private Set<Resposta> respostas = new LinkedHashSet<>();

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
    private Set<Subtema> subtemas = new LinkedHashSet<>();


    @OneToMany(
        mappedBy = "questao",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Associações entre a questão e cargos do concurso")
    private Set<QuestaoCargo> questaoCargos = new LinkedHashSet<>();

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

    public Set<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(Set<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }

    public Boolean getDesatualizada() {
        return desatualizada;
    }

    public void setDesatualizada(Boolean desatualizada) {
        this.desatualizada = desatualizada;
    }

    public Set<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(Set<Resposta> respostas) {
        this.respostas = respostas;
    }

    public Set<Subtema> getSubtemas() {
        return subtemas;
    }

    public void setSubtemas(Set<Subtema> subtemas) {
        this.subtemas = subtemas;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : null;
    }

    public Set<QuestaoCargo> getQuestaoCargos() {
        return questaoCargos;
    }

    public void setQuestaoCargos(Set<QuestaoCargo> questaoCargos) {
        this.questaoCargos = questaoCargos;
    }

    public void addQuestaoCargo(QuestaoCargo questaoCargo) {
        this.questaoCargos.add(questaoCargo);
        questaoCargo.setQuestao(this);
    }
}
