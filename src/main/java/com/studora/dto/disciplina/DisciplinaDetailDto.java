package com.studora.dto.disciplina;

import com.studora.dto.tema.TemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "DTO detalhado de Disciplina")
public class DisciplinaDetailDto {
    private Long id;
    private String nome;
    private List<TemaSummaryDto> temas;
}