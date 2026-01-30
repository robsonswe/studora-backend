package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "alternativa",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "questao_id", "ordem" }),
    },
    indexes = {
        @Index(name = "idx_alternativa_questao", columnList = "questao_id"),
        @Index(
            name = "idx_alternativa_correta",
            columnList = "questao_id, correta"
        ),
    }
)
@Schema(description = "Entidade que representa uma alternativa de uma questão")
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da alternativa", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    @Schema(description = "Questão à qual a alternativa pertence")
    private Questao questao;

    @Column(nullable = false)
    @Schema(description = "Ordem da alternativa na lista", example = "1")
    private Integer ordem;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Texto da alternativa", example = "A resposta correta é a opção A")
    private String texto;

    @Column(nullable = false)
    @Schema(description = "Indica se a alternativa é a correta", example = "true")
    private Boolean correta;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Justificativa da alternativa", example = "Esta é a alternativa correta porque...")
    private String justificativa;

    @OneToMany(
        mappedBy = "alternativaEscolhida",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Schema(description = "Respostas associadas a esta alternativa")
    private List<Resposta> respostas;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "alternativa_imagem",
        joinColumns = @JoinColumn(name = "alternativa_id"),
        inverseJoinColumns = @JoinColumn(name = "imagem_id"),
        indexes = {
            @Index(
                name = "idx_alternativa_imagem_imagem",
                columnList = "imagem_id"
            ),
        }
    )
    @Schema(description = "Imagens associadas à alternativa")
    private List<Imagem> imagens;

    public Alternativa() {}

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
