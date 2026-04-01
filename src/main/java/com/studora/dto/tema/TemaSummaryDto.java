package com.studora.dto.tema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de temas")
@Data
public class TemaSummaryDto {
    @Schema(description = "ID único do tema", example = "1")
    private Long id;

    @Schema(description = "ID da disciplina à qual o tema pertence", example = "1")
    private Long disciplinaId;

    @Schema(description = "Nome da disciplina à qual o tema pertence", example = "Direito Constitucional")
    private String disciplinaNome;

    @Schema(description = "Nome do tema", example = "Direitos Fundamentais")
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para todos os subtemas deste tema", example = "5")
    private long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas deste tema")
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Total de subtemas neste tema", example = "3")
    private long totalSubtemas;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "2")
    private long subtemasEstudados;
}
