package com.studora.dto.tema;

import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Schema(description = "DTO detalhado para visualização de um tema")
@Data
public class TemaDetailDto {
    private Long id;
    private DisciplinaSummaryDto disciplina;
    private String nome;
    private List<SubtemaSummaryDto> subtemas;
}