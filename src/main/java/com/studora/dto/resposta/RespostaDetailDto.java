package com.studora.dto.resposta;

import com.studora.dto.questao.AlternativaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO detalhado de resposta incluindo alternativas da questão")
public class RespostaDetailDto {
    private Long id;
    private Long questaoId;
    private Long alternativaId;
    private Boolean correta;
    private String justificativa;
    private Integer tempoRespostaSegundos;
    private Long simuladoId;
    private LocalDateTime createdAt;
    
    @Schema(description = "Lista de alternativas da questão para contexto")
    private List<AlternativaDto> alternativas;
}
