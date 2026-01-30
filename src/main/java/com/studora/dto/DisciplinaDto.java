package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para representar uma disciplina")
@Data
public class DisciplinaDto {

    @Schema(description = "ID único da disciplina", example = "1")
    private Long id;

    @NotBlank(message = "Nome da disciplina é obrigatório")
    @Size(max = 255, message = "Nome da disciplina deve ter no máximo 255 caracteres")
    @Schema(description = "Nome da disciplina", example = "Matemática", required = true)
    private String nome;

    // Constructors
    public DisciplinaDto() {}

    public DisciplinaDto(String nome) {
        this.nome = nome;
    }
}