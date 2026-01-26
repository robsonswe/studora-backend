package com.studora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AlternativaDto {
    
    private Long id;
    
    @NotNull(message = "ID da questão é obrigatório")
    private Long questaoId;
    
    @NotNull(message = "Ordem é obrigatória")
    @Positive(message = "Ordem deve ser um número positivo")
    private Integer ordem;
    
    @NotBlank(message = "Texto da alternativa é obrigatório")
    private String texto;
    
    @NotNull(message = "Indicação de correta é obrigatória")
    private Boolean correta;
    
    private String justificativa;
    
    // Constructors
    public AlternativaDto() {}
    
    public AlternativaDto(Long questaoId, Integer ordem, String texto, Boolean correta) {
        this.questaoId = questaoId;
        this.ordem = ordem;
        this.texto = texto;
        this.correta = correta;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getQuestaoId() {
        return questaoId;
    }
    
    public void setQuestaoId(Long questaoId) {
        this.questaoId = questaoId;
    }
    
    public Integer getOrdem() {
        return ordem;
    }
    
    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
    
    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    public Boolean getCorreta() {
        return correta;
    }
    
    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }
    
    public String getJustificativa() {
        return justificativa;
    }
    
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
}