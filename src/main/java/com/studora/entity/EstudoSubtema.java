package com.studora.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "estudo_subtema",
    indexes = {
        @Index(name = "idx_estudo_subtema_subtema", columnList = "subtema_id"),
        @Index(name = "idx_estudo_subtema_created_at", columnList = "created_at")
    }
)
@Schema(description = "Entidade que representa uma sessão de estudo de um subtema")
public class EstudoSubtema extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da sessão de estudo", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtema_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "Subtema estudado")
    private Subtema subtema;

    public EstudoSubtema() {}

    public EstudoSubtema(Subtema subtema) {
        this.subtema = subtema;
    }
}
