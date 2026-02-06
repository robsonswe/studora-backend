package com.studora.dto.instituicao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO detalhado para visualização de uma instituição")
@Data
public class InstituicaoDetailDto {
    private Long id;
    private String nome;
    private String area;
}
