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
        @Index(name = "idx_resposta_data", columnList = "respondidaEm"),
    }
)
@Schema(description = "Entidade que representa uma resposta a uma questão")
public class Resposta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da resposta", example = "1")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false, unique = true)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Questão respondida")
    private Questao questao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternativa_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Alternativa escolhida como resposta")
    private Alternativa alternativaEscolhida;

    @Column(nullable = false)
    @Schema(description = "Data e hora em que a resposta foi registrada", example = "2023-06-15T10:30:00")
    private LocalDateTime respondidaEm = LocalDateTime.now();

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

    public LocalDateTime getRespondidaEm() {
        return respondidaEm;
    }

    public void setRespondidaEm(LocalDateTime respondidaEm) {
        this.respondidaEm = respondidaEm;
    }
}
