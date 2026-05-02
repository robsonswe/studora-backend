package com.studora.dto.prova;

import lombok.Data;
import java.util.List;

@Data
public class ProvaDetailDto {
    private Long id;
    private Long concursoId;
    private String nome;
       
    private Long cargoId;
    private List<ProvaSecaoDto> secoes;
}