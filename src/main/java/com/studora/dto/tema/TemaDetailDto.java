package com.studora.dto.tema;

import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Schema(description = "DTO detalhado para visualização de um tema")
@Data
public class TemaDetailDto {
    private Long id;
    private DisciplinaSummaryDto disciplina;
    private String nome;
    private List<SubtemaSummaryDto> subtemas;

    @Schema(description = "Total de sessões de estudo realizadas para todos os subtemas deste tema", example = "5")
    private long totalEstudos;

    @Schema(description = "Data e hora do último estudo realizado entre todos os subtemas deste tema")
    private java.time.LocalDateTime ultimoEstudo;

    @Schema(description = "Total de subtemas neste tema", example = "3")
    private long totalSubtemas;

    @Schema(description = "Número de subtemas que possuem pelo menos uma sessão de estudo", example = "2")
    private long subtemasEstudados;
}