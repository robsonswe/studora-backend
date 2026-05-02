package com.studora.dto.concurso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class ConcursoSecaoDto {
    private Long id;
    private String nome;
    private Integer ordem;
    private Integer numQuestoes;
    private Double peso;
    private Double notaMinima;
    private List<ConcursoCargoSubtemaDto> assuntos;
}
