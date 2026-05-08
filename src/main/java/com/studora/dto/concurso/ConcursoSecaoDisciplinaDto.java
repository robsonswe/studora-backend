package com.studora.dto.concurso;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
public class ConcursoSecaoDisciplinaDto {
    private Long id;
    private String nome;
    private Integer numQuestoes;
    private Double peso;
    private Double notaMinima;
    private List<ConcursoCargoSubtemaDto> assuntos;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalEstudos;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private com.studora.dto.QuestaoStatsDto questaoStats;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private com.studora.dto.StatSliceDto questoesConcursoCargo;
}
