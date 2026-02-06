package com.studora.dto.banca;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO detalhado para visualização de uma banca")
@Data
public class BancaDetailDto {
    private Long id;
    private String nome;
}
