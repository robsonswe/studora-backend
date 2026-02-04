package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO para criação de uma disciplina")
@Data
public class DisciplinaCreateRequest {

    @NotBlank(message = "Nome da disciplina é obrigatório")
    @Size(max = AppConstants.MAX_NAME_LENGTH, message = "Nome da disciplina deve ter no máximo " + AppConstants.MAX_NAME_LENGTH + " caracteres")
    @Schema(description = "Nome da disciplina", example = "Matemática", required = true)
    private String nome;

    // Constructors
    public DisciplinaCreateRequest() {}

    public DisciplinaCreateRequest(String nome) {
        this.nome = nome;
    }
}