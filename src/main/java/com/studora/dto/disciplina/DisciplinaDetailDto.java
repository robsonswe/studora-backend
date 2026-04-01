package com.studora.dto.disciplina;

import com.studora.dto.tema.TemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "DTO detalhado de Disciplina")
public class DisciplinaDetailDto {
    private Long id;
    private String nome;
    private List<TemaSummaryDto> temas;

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