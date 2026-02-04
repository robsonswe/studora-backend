package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO para criação de um subtema")
@Data
public class SubtemaCreateRequest {

    @NotNull(message = "ID do tema é obrigatório")
    @Schema(description = "ID do tema ao qual o subtema pertence", example = "1", required = true)
    private Long temaId;

    @NotBlank(message = "Nome do subtema é obrigatório")
    @Size(max = AppConstants.MAX_NAME_LENGTH, message = "Nome do subtema deve ter no máximo " + AppConstants.MAX_NAME_LENGTH + " caracteres")
    @Schema(description = "Nome do subtema", example = "Equações de primeiro grau", required = true)
    private String nome;

    // Constructors
    public SubtemaCreateRequest() {}

    public SubtemaCreateRequest(Long temaId, String nome) {
        this.temaId = temaId;
        this.nome = nome;
    }
}