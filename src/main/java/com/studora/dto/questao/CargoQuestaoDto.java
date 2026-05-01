package com.studora.dto.questao;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.entity.NivelCargo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "DTO de cargo no contexto de uma questão, incluindo a seção da prova")
public class CargoQuestaoDto {
    @Schema(description = "ID do cargo", example = "1")
    @JsonView(Views.Summary.class)
    private Long id;
    @Schema(description = "Nome do cargo", example = "Analista Judiciário")
    @JsonView(Views.Summary.class)
    private String nome;
    @Schema(description = "Nível de escolaridade", example = "SUPERIOR")
    @JsonView(Views.Summary.class)
    private NivelCargo nivel;
    @Schema(description = "Área de atuação", example = "JUDICIARIA")
    @JsonView(Views.Summary.class)
    private String area;
    @Schema(description = "Seções da prova em que a questão aparece para este cargo")
    @JsonView(Views.Summary.class)
    private List<SecaoQuestaoDto> secoes;
}
