package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO para criação de uma instituição")
@Data
public class InstituicaoCreateRequest {

    @NotBlank(message = "Nome da instituição é obrigatório")
    @Size(max = 255, message = "Nome da instituição deve ter no máximo 255 caracteres")
    @Schema(description = "Nome da instituição", example = "Universidade Federal do Rio de Janeiro", required = true)
    private String nome;

    // Constructors
    public InstituicaoCreateRequest() {}

    public InstituicaoCreateRequest(String nome) {
        this.nome = nome;
    }
}