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
@Table(name = "prova_secao_peso")
@Getter
@Setter
public class ProvaSecaoPeso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prova_secao_id", nullable = false)
    private ProvaSecao provaSecao;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_cargo_id")
    private ConcursoCargo concursoCargo;
    @Column(nullable = false)
    private Double peso = 1.0;
    @Column(name = "nota_minima")
    private Double notaMinima;
}
