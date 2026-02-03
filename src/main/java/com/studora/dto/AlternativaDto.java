package com.studora.dto;

import com.studora.dto.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "DTO para representar uma alternativa de questão")
public class AlternativaDto {
    
    @Schema(description = "ID da alternativa (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonView(Views.RespostaOculta.class)
    private Long id;

    @Schema(description = "ID da questão à qual a alternativa pertence (definido pelo parâmetro da URL)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonView(Views.RespostaOculta.class)
    private Long questaoId;

    @Schema(description = "Ordem da alternativa na lista", example = "1", required = true)
    @NotNull(message = "Ordem é obrigatória")
    @Positive(message = "Ordem deve ser um número positivo")
    @JsonView(Views.RespostaOculta.class)
    private Integer ordem;

    @Schema(description = "Texto da alternativa", example = "A resposta correta é a opção A", required = true)
    @NotBlank(message = "Texto da alternativa é obrigatório")
    @JsonView(Views.RespostaOculta.class)
    private String texto;

    @Schema(description = "Indica se a alternativa é a correta", example = "true", required = true)
    @NotNull(message = "Indicação de correta é obrigatória")
    @JsonView(Views.RespostaVisivel.class)
    private Boolean correta;

    @Schema(description = "Justificativa da alternativa", example = "Esta é a alternativa correta porque...")
    @JsonView(Views.RespostaVisivel.class)
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