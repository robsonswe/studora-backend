package com.studora.dto.request;

public interface AlternativaBaseRequest {
    Integer getOrdem();
    String getTexto();
    Boolean getCorreta();
    String getJustificativa();
}
