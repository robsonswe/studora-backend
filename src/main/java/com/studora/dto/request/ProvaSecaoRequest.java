package com.studora.dto.request;

import java.util.List;

public interface ProvaSecaoRequest {
    String getNome();
    Integer getOrdem();
    Integer getNumQuestoes();
    Double getPeso();
    Double getNotaMinima();
    List<SecaoDisciplinaRequest> getDisciplinas();
}
