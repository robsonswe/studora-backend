package com.studora.dto.prova;
import lombok.Data;
@Data
public class ProvaSecaoPesoDto {
    private Long id;
    private Long concursoCargoId;
    private Double peso;
    private Double notaMinima;
}
