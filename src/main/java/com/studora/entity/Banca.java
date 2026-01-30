package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Schema(description = "Entidade que representa uma banca organizadora de concursos")
public class Banca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da banca", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nome da banca organizadora", example = "CESPE")
    private String nome;
}
