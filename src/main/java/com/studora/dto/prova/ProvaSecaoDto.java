package com.studora.dto.prova;

import lombok.Data;
import java.util.List;

@Data
public class ProvaSecaoDto {
    private Long id;
    private String nome;
    private Integer ordem;
    private Integer numQuestoes;
    private List<ProvaSecaoPesoDto> pesos;
    private List<Long> subtemaIds;
}