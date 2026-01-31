package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO para atualização de um subtema")
@Data
public class SubtemaUpdateRequest {

    @NotNull(message = "ID do tema é obrigatório")
    @Schema(description = "ID do tema ao qual o subtema pertence", example = "1", required = true)
    private Long temaId;

    @NotBlank(message = "Nome do subtema é obrigatório")
    @Size(max = 255, message = "Nome do subtema deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do subtema", example = "Equações de primeiro grau", required = true)
    private String nome;

    // Constructors
    public SubtemaUpdateRequest() {}

    public SubtemaUpdateRequest(Long temaId, String nome) {
        this.temaId = temaId;
        this.nome = nome;
    }
}