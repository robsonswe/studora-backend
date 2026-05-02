package com.studora.dto.prova;

import lombok.Data;

@Data
public class ProvaSummaryDto {
    private Long id;
    private Long concursoId;
    private Long cargoId;
    private String nome;
}