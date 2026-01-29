package com.studora.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RespostaDto {
    
    private Long id;
    
    @NotNull(message = "ID da questão é obrigatório")
    private Long questaoId;
    
    @NotNull(message = "ID da alternativa é obrigatório")
    private Long alternativaId;
    
    private LocalDateTime respondidaEm;
    
    // Constructors
    public RespostaDto() {}
    
    public RespostaDto(Long questaoId, Long alternativaId) {
        this.questaoId = questaoId;
        this.alternativaId = alternativaId;
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
    
    public Long getAlternativaId() {
        return alternativaId;
    }
    
    public void setAlternativaId(Long alternativaId) {
        this.alternativaId = alternativaId;
    }
    
    public LocalDateTime getRespondidaEm() {
        return respondidaEm;
    }
    
    public void setRespondidaEm(LocalDateTime respondidaEm) {
        this.respondidaEm = respondidaEm;
    }
}