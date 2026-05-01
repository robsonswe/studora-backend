package com.studora.dto.request;

import lombok.Data;

@Data
public class ProvaSecaoPesoUpdateRequest {
    private Long id;
    private Long cargoId;
    private Double peso;
    private Double notaMinima;
}
