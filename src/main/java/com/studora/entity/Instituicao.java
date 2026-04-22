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
@Schema(description = "Entidade que representa uma instituição organizadora ou contratante")
public class Instituicao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da instituição", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nome da instituição", example = "Universidade Federal do Rio de Janeiro")
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

    @Column(nullable = false)
    @Schema(description = "Área da instituição", example = "Educação")
    private String area;

    @Column(name = "area_normalized")
    @Schema(hidden = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String areaNormalized;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instituicao)) return false;
        Instituicao that = (Instituicao) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
