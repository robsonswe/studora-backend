package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO para criação de uma disciplina")
@Data
public class DisciplinaCreateRequest {

    @NotBlank(message = "Nome da disciplina é obrigatório")
    @Size(max = 255, message = "Nome da disciplina deve ter no máximo 255 caracteres")
    @Schema(description = "Nome da disciplina", example = "Matemática", required = true)
    private String nome;

    // Constructors
    public DisciplinaCreateRequest() {}

    public DisciplinaCreateRequest(String nome) {
        this.nome = nome;
    }
}