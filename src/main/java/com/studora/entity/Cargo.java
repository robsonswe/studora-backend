package com.studora.entity;

import com.studora.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

    @Column(name = "nome_normalized")
    @Schema(hidden = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String nomeNormalized;

    @PrePersist
    @PreUpdate
    public void normalize() {
        this.nomeNormalized = StringUtils.normalizeForSearch(this.nome);
        this.areaNormalized = StringUtils.normalizeForSearch(this.area);
    }

    @Enumerated(EnumType.STRING)
    @Schema(description = "Nível do cargo", example = "SUPERIOR")
    private NivelCargo nivel;

    @Schema(description = "Área do cargo", example = "Tecnologia da Informação")
    private String area;

    @Column(name = "area_normalized")
    @Schema(hidden = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String areaNormalized;

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
