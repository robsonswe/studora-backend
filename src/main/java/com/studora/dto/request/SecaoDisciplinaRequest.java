package com.studora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SecaoDisciplinaRequest {
    private Long id;

    @NotBlank(message = "Nome da disciplina é obrigatório")
    private String nome;

    private Integer numQuestoes;
    private Double peso;
    private Double notaMinima;
    private List<Long> subtemaIds;
}
