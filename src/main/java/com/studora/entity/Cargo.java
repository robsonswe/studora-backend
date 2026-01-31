package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
@Schema(description = "Entidade que representa um cargo")
public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do cargo", example = "1")
    private Long id;

    @Schema(description = "Nome do cargo", example = "Analista de Sistemas")
    private String nome;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Nível do cargo", example = "SUPERIOR")
    private NivelCargo nivel;

    @Schema(description = "Área do cargo", example = "Tecnologia da Informação")
    private String area;
}
