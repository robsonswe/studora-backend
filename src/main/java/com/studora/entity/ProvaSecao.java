package com.studora.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prova_secao")
@Getter
@Setter
public class ProvaSecao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prova_id", nullable = false)
    private Prova prova;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "num_questoes")
    private Integer numQuestoes;

    @OneToMany(mappedBy = "provaSecao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProvaSecaoPeso> pesos = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "prova_secao_subtema",
            joinColumns = @JoinColumn(name = "prova_secao_id"),
            inverseJoinColumns = @JoinColumn(name = "subtema_id")
    )
    private Set<Subtema> subtemas = new LinkedHashSet<>();

    public void addPeso(ProvaSecaoPeso peso) {
        this.pesos.add(peso);
        peso.setProvaSecao(this);
    }

    public void addSubtema(Subtema subtema) {
        this.subtemas.add(subtema);
    }
}