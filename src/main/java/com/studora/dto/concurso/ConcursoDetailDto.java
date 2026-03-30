package com.studora.dto.concurso;

import com.studora.dto.instituicao.InstituicaoSummaryDto;
import com.studora.dto.banca.BancaSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Schema(description = "DTO detalhado para visualização de um concurso")
@Data
public class ConcursoDetailDto {
    private Long id;
    private InstituicaoSummaryDto instituicao;
    private BancaSummaryDto banca;
    private Integer ano;
    private Integer mes;
    private String edital;
    private List<ConcursoCargoSummaryDto> cargos;
    
    @Schema(description = "Indica se o usuário está inscrito em algum cargo deste concurso. Pode ser 'false' ou um objeto '{\"cargo\": cargoId}'", example = "{\"cargo\": 1}")
    private Object inscrito;
}