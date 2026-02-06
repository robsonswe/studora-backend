package com.studora.dto.concurso;

import com.studora.dto.instituicao.InstituicaoSummaryDto;
import com.studora.dto.banca.BancaSummaryDto;
import com.studora.dto.concurso.ConcursoCargoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Schema(description = "DTO detalhado para visualização de um concurso")
@Data
public class ConcursoDetailDto {
    private Long id;
    private Long instituicaoId;
    private Long bancaId;
    private InstituicaoSummaryDto instituicao;
    private BancaSummaryDto banca;
    private Integer ano;
    private Integer mes;
    private String edital;
    private List<ConcursoCargoDto> cargos;
}
