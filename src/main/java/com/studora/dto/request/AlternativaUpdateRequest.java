package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request DTO para atualizar uma alternativa de uma questão")
public class AlternativaUpdateRequest {

    @Schema(description = "Ordem da alternativa na lista", example = "1", required = true)
    @NotNull(message = "Ordem é obrigatória")
    @Positive(message = "Ordem deve ser um número positivo")
    private Integer ordem;

    @Schema(description = "Texto da alternativa", example = "A resposta correta é a opção A", required = true)
    @NotBlank(message = "Texto da alternativa é obrigatório")
    private String texto;

    @Schema(description = "Indica se a alternativa é a correta", example = "true", required = true)
    @NotNull(message = "Indicação de correta é obrigatória")
    private Boolean correta;

    @Schema(description = "Justificativa da alternativa", example = "Esta é a alternativa correta porque...")
    private String justificativa;

    // Constructors
    public AlternativaUpdateRequest() {}

    public AlternativaUpdateRequest(Integer ordem, String texto, Boolean correta) {
        this.ordem = ordem;
        this.texto = texto;
        this.correta = correta;
    }

    // Getters and Setters
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