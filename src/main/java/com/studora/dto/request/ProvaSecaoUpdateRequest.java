package com.studora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ProvaSecaoUpdateRequest implements ProvaSecaoRequest {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private Integer ordem;
    private Integer numQuestoes;

    private Double peso;
    private Double notaMinima;
    private List<SecaoDisciplinaRequest> disciplinas;
}