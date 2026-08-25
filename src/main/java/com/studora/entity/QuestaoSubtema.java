package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "questao_subtema",
    uniqueConstraints = @UniqueConstraint(columnNames = {"questao_id", "subtema_id"})
)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"questao", "subtema"})
@Schema(description = "Relacionamento entre questão e subtema, com indicação de subtema principal")
public class QuestaoSubtema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtema_id", nullable = false)
    private Subtema subtema;

    @Column(nullable = false)
    @Schema(description = "Indica se este é o subtema principal da questão para fins de vinculação ao edital", example = "true")
    private Boolean principal = false;

    public QuestaoSubtema(Questao questao, Subtema subtema, Boolean principal) {
        this.questao = questao;
        this.subtema = subtema;
        this.principal = principal;
    }
}
