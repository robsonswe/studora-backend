package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para representar uma disciplina")
@Data
public class DisciplinaDto {

    @Schema(description = "ID único da disciplina (gerado automaticamente)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Nome da disciplina é obrigatório")
    @Size(max = AppConstants.MAX_NAME_LENGTH, message = "Nome da disciplina deve ter no máximo " + AppConstants.MAX_NAME_LENGTH + " caracteres")
    @Schema(description = "Nome da disciplina", example = "Matemática", required = true)
    private String nome;

    // Constructors
    public DisciplinaDto() {}

    public DisciplinaDto(String nome) {
        this.nome = nome;
    }
}