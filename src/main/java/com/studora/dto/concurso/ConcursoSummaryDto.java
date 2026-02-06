package com.studora.dto.concurso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO simplificado para listagem de concursos")
@Data
public class ConcursoSummaryDto {
    private Long id;
    private Long instituicaoId;
    private Long bancaId;
    private Integer ano;
    private Integer mes;
    private String edital;
}
