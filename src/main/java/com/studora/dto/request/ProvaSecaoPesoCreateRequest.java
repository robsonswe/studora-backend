package com.studora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ProvaSecaoPesoCreateRequest {
    private Long cargoId;
    private Double peso;
    private Double notaMinima;
}
