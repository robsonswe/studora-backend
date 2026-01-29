package com.studora.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Banca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;
}
