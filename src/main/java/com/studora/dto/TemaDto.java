package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para representar um tema")
@Data
public class TemaDto {

    @Schema(description = "ID único do tema", example = "1")
    private Long id;

    @NotNull(message = "ID da disciplina é obrigatório")
    @Schema(description = "ID da disciplina à qual o tema pertence", example = "1", required = true)
    private Long disciplinaId;

    @NotBlank(message = "Nome do tema é obrigatório")
    @Size(max = 255, message = "Nome do tema deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do tema", example = "Álgebra", required = true)
    private String nome;

    // Constructors
    public TemaDto() {}

    public TemaDto(Long disciplinaId, String nome) {
        this.disciplinaId = disciplinaId;
        this.nome = nome;
    }
}