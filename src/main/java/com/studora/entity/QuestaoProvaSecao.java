package com.studora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "questao_prova_secao")
@Getter
@Setter
public class QuestaoProvaSecao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prova_secao_id", nullable = false)
    private ProvaSecao provaSecao;
    @Column(name = "numero_questao")
    private Integer numeroQuestao;
}
