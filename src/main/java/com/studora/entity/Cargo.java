package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Schema(description = "Entidade que representa um cargo público")
public class Cargo extends BaseEntity {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cargo)) return false;
        Cargo cargo = (Cargo) o;
        return id != null && id.equals(cargo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
