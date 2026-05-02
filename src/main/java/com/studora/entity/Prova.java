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
@Table(name = "prova")
@Getter
@Setter
public class Prova extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_id", nullable = false)
    private Concurso concurso;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concurso_cargo_id")
    private ConcursoCargo concursoCargo;
    
    @OneToMany(mappedBy = "prova", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProvaSecao> secoes = new LinkedHashSet<>();

    public void addSecao(ProvaSecao secao) {
        this.secoes.add(secao);
        secao.setProva(this);
    }
}