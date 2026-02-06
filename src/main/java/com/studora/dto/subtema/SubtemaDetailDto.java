package com.studora.dto.subtema;

import com.studora.dto.tema.TemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO detalhado para visualização de um subtema")
@Data
public class SubtemaDetailDto {
    private Long id;
    private TemaSummaryDto tema;
    private String nome;
}
