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
@Schema(description = "Entidade que representa uma banca organizadora de concursos")
public class Banca extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da banca", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nome da banca organizadora", example = "CESPE")
    private String nome;

    @Column(name = "nome_normalized")
    @Schema(hidden = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String nomeNormalized;

    @PrePersist
    @PreUpdate
    public void normalize() {
        this.nomeNormalized = StringUtils.normalizeForSearch(this.nome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Banca)) return false;
        Banca banca = (Banca) o;
        return id != null && id.equals(banca.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
