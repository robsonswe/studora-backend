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
    @Schema(description = "ID único da resposta", example = "1")
    private Long id;

    @Schema(description = "ID da questão respondida", example = "1")
    private Long questaoId;

    @Schema(description = "ID da alternativa selecionada", example = "1")
    private Long alternativaId;

    @Schema(description = "Indica se a resposta foi correta", example = "true")
    private Boolean correta;

    @Schema(description = "Justificativa da resposta", example = "Raciocínio lógico...")
    private String justificativa;

    @Schema(description = "Tempo levado para responder em segundos", example = "45")
    private Integer tempoRespostaSegundos;

    @Schema(description = "ID do simulado ao qual a resposta pertence (opcional)", example = "1")
    private Long simuladoId;

    @Schema(description = "Grau de dificuldade percebido pelo usuário")
    private com.studora.entity.Dificuldade dificuldade;

    @Schema(description = "Data e hora em que a resposta foi registrada", example = "2023-06-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Lista de alternativas da questão para contexto")
    private List<AlternativaDto> alternativas;
}
