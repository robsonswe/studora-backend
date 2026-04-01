package com.studora.dto.disciplina;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de disciplinas")
@Data
public class DisciplinaSummaryDto {
    @Schema(description = "ID único da disciplina", example = "1")
    private Long id;

    @Schema(description = "Nome da disciplina", example = "Direito Constitucional")
    private String nome;

    @Schema(description = "Total de sessões de estudo realizadas para todos os subtemas desta disciplina", example = "10")
    private long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas desta disciplina")
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Total de temas nesta disciplina", example = "5")
    private long totalTemas;

    @Schema(description = "Total de subtemas nesta disciplina", example = "12")
    private long totalSubtemas;

    @Schema(description = "Número de temas onde todos os subtemas possuem pelo menos uma sessão de estudo", example = "2")
    private long temasEstudados;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "8")
    private long subtemasEstudados;
}
