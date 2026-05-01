package com.studora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ProvaSecaoCreateRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private Integer ordem;
    private Integer numQuestoes;

    private List<ProvaSecaoPesoCreateRequest> pesos;
    private List<Long> subtemaIds;
}