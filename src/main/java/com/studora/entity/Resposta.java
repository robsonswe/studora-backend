package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "resposta",
    indexes = {
        @Index(
            name = "idx_resposta_alternativa",
            columnList = "alternativa_id"
        ),
        @Index(name = "idx_resposta_questao", columnList = "questao_id"),
        @Index(name = "idx_resposta_created_at", columnList = "created_at"),
    }
)
@Schema(description = "Entidade que representa uma resposta a uma questão")
public class Resposta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da resposta", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Questão respondida")
    private Questao questao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternativa_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Alternativa escolhida como resposta")
    private Alternativa alternativaEscolhida;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Raciocínio ou comentário do usuário para esta tentativa", example = "Achei que era a B por causa de...")
    private String justificativa;

    @Convert(converter = DificuldadeConverter.class)
    @Column(name = "dificuldade_id")
    @Schema(description = "Grau de dificuldade percebido pelo usuário (1=Fácil, 2=Média, 3=Difícil, 4=Chute)")
    private Dificuldade dificuldade;

    @Column(name = "tempo_resposta_segundos")
    @Schema(description = "Duração da tentativa em segundos", example = "45")
    private Integer tempoRespostaSegundos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulado_id")
    @Schema(description = "Simulado ao qual esta resposta pertence (opcional)")
    private Simulado simulado;

    // Constructors
    public Resposta() {}

    public Resposta(Questao questao, Alternativa alternativaEscolhida) {
        this.questao = questao;
        this.alternativaEscolhida = alternativaEscolhida;
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

    public Alternativa getAlternativaEscolhida() {
        return alternativaEscolhida;
    }

    public void setAlternativaEscolhida(Alternativa alternativaEscolhida) {
        this.alternativaEscolhida = alternativaEscolhida;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(Dificuldade dificuldade) {
        this.dificuldade = dificuldade;
    }

    public Integer getTempoRespostaSegundos() {
        return tempoRespostaSegundos;
    }

    public void setTempoRespostaSegundos(Integer tempoRespostaSegundos) {
        this.tempoRespostaSegundos = tempoRespostaSegundos;
    }

    public Simulado getSimulado() {
        return simulado;
    }

    public void setSimulado(Simulado simulado) {
        this.simulado = simulado;
    }
}
