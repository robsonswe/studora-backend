package com.studora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatSliceDto {
    private Long id;
    private String nome;
    private Long respondidas;
    private Long acertadas;
    private Long totalQuestoes;
    private Integer mediaTempoResposta;
    private Map<String, DificuldadeStatDto> dificuldade;
    private LocalDateTime ultimaQuestao;
}
